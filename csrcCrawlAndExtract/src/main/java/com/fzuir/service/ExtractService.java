package com.fzuir.service;

import com.fzuir.domain.HtmlContent;
import com.fzuir.domain.PostData;
import com.fzuir.domain.Source;
import com.fzuir.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.crypto.spec.PSource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ExtractService {
    private final static String USERAGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36";
    //    private static final int TIMEOUT = Integer.valueOf(Configuration.getProperty("connect.timeout"));
    private static final int TIMEOUT = 80000;
    private static final String  SEARCHURL = "http://www.csrc.gov.cn/wcm/websearch/zjh_simp_list.jsp";

    public void start() {
        // 分两种情况，一种是来源页就有按行政处罚等文书类型分好的信息列表，还有一种是需要通过搜索栏搜索行政处罚文书等关键字来获得信息列表



        /* 情况二: 通过搜索栏搜索行政处罚文书等关键字来获得信息列表 */
        // 从数据库获得搜索的关键字和来源标识（用来放到Post请求的form data中）
        List<PostData> postDataList = DBUtil.getPostData();
        for (PostData postData: postDataList) {
            BlockingQueue<HtmlContent> pathQueue = getUrlsBySearch(postData);
            collect(pathQueue);
        }

        /* 情况一：直接从来源页获得待采集url列表 */
        // 从数据库读取所有来源页url
        List<Source> sources = DBUtil.getSource();
        // 遍历所有来源页，先抽取出一个来源页的所有待采集url，再去采集
        for (Source source : sources) {
            // 通过key值获取value
            BlockingQueue<HtmlContent> pathQueue = getPathQueue(source);
            collect(pathQueue);
        }
    }

    /**
     * 通过搜索来获得信息列表，需要搜索关键词和来源标识（标识哪个站点）
     * @param postData
     * @return
     */
    public BlockingQueue<HtmlContent> getUrlsBySearch(final PostData postData) {
        log.info("---------------------begin to extract urls by search words: " + postData);
//        final int BEFOREDAY = Integer.valueOf(Configuration.getProperty("extract.before.days"));
        final int BEFOREDAY = -1;
        final String BASEURL = "http://www.csrc.gov.cn";

        BlockingQueue<HtmlContent> urlQueue = new LinkedBlockingDeque<>();
        final String schword =  postData.getSchword();
        final String searchword =  postData.getSearchword();
        int page = 1;
        int count = 0;
        int resultCount = 0;
        Document doc = null;
        Connection con = null;
        try {

            while (true) {
                con = Jsoup.connect(SEARCHURL)
                        .timeout(TIMEOUT)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
                // 设置搜索参数
                con.data("schword", schword);
                con.data("searchword", searchword);
                con.data("page", String.valueOf(page));

                doc = con.post();
                // 在第一页获取的时候顺便获取检索的总条数
                if (page == 1) {
                    String tempCount = doc.select("div.tishi").text().replaceAll("[^0-9]", "");
                    if (StringUtil.isBlank(tempCount)) {
                        log.info("检索结果记录数获取失败");
                        break;
                    }
                    resultCount = Integer.valueOf(tempCount);
                }
                Elements elements = doc.select("div.jieguolist");
                for (Element element: elements) {
                    count ++;
                    Element aEle = element.selectFirst("a");
                    String title = aEle.text();
                    // 标题和正文里面包含关键字的选项都会被搜索出来，接下来只取标题里包含关键字的选项
                    // TODO
                    if (title.indexOf(schword) == -1) {
                        continue;
                    }
                    // 检查是否在需要检索的有效期内
                    String tempDate = element.selectFirst("div.fileinfo").text();
                    String releaseDate = DateUtil.cleanDate(tempDate);
                    if (DateUtil.checkDate(releaseDate, BEFOREDAY)) {
                        String path = aEle.attr("href");
                        HtmlContent html = new HtmlContent(BASEURL + path, title, null,
                                null, releaseDate, postData.getFrom(), postData.getLibraryType());
                        urlQueue.add(html);
                        log.info("add url to urlQueue: " + BASEURL + path);
                    }
                }
                // 直接通过比较遍历的个数和总检索数来判断是否遍历结束
                if (count >= resultCount) {
                    log.info("urls extraction completed");
                    break;
                }
                log.info("page " + page + " extraction completed");
                page ++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return urlQueue;
    }


    /**
     * 给定一个来源页，从他的选项链接中抽取真正要爬取页面的url,在获得url同时能够得到标题和时间
     * @param source 来源
     * @return 将url和其他信息包装成HtmlContent对象返回
     */
    public BlockingQueue<HtmlContent> getPathQueue(final Source source) {
        log.info("---------------------begin to extract urls from source: {" + source.getUrl() + " }");
        final int BEFOREDAY = Integer.valueOf(-1);
        int curPage = 0;
        int pageCount = -1;
        boolean nextPage = true;
        final String sourceUrl = source.getUrl();
        String url = source.getUrl();
        BlockingQueue<HtmlContent> urlQueue = new LinkedBlockingDeque<>();
        Document doc;
        try {
            while (nextPage) {
                curPage ++;
                doc = Jsoup.connect(url).userAgent(USERAGENT).get();
                Elements elements = doc.select("div.fl_list > ul > li");
                // 列表展示有两种情况要分开处理
                if (elements.size() != 0) {
                    // 第一种情况能够从html中抽取出总页数，在准备爬取第一页url时候获得就行
                    if (curPage == 1) {
                        pageCount = extractPageCount(doc);
                    }
                    // 第一种情况有bug，按照规则加页数获得新的来源页面时，有的来源有可能出现超出总页数会跳到无关的来源列表页
                    if (curPage == pageCount) {
                        break;
                    }

                    for (Element element : elements) {
                        String releaseDate = DateUtil.cleanDate(element.selectFirst("span").text());
                        if (DateUtil.checkDate(releaseDate, BEFOREDAY)) {
                            Element aElement = element.selectFirst("a");
                            String path = aElement != null ? aElement.attr("href") : "";
                            if (StringUtil.isBlank(path)) continue;
                            if (path.charAt(0) == '.') {
                                path = path.replaceFirst(".", "");
                            }
                            // 将所有已知的信息保存到htmlContent,包括：url, title， data， from， libtype
                            HtmlContent html = new HtmlContent(sourceUrl + path, aElement.text(), null,
                                    null, releaseDate, source.getFrom(), source.getLibraryType());
                            urlQueue.add(html);
                        } else {
                            // 因为信息是按照日期排序的，最新的都在前面，一旦碰到待爬取日期外的连接，就表示之后的内容都是不需要爬取，可以直接推出循环
                            nextPage = false;
                            break;
                        }
                    }
                    // 从第二页开始，url路径会在index加数字
                    url = sourceUrl + "/index_" + curPage +".html";
                } else {
                    // 第二种情况的来源类似：http://www.csrc.gov.cn/pub/zjhpublic/3300/3313/index_7401_1.htm
                    // 抽取完路径后是需要和http://www.csrc.gov.cn/pub/zjhpublic这一部分拼接
                    int indexTemp = sourceUrl.indexOf("pub");
                    if (indexTemp == -1) continue;
                    int startIndex = sourceUrl.indexOf("/", indexTemp + 4);
                    String host = sourceUrl.substring(0, startIndex);

                    elements = doc.select("div.row");
                    for (Element element: elements) {
                        String releaseDate = DateUtil.cleanDate(element.select(".fbrq").get(0).text());
                        if (DateUtil.checkDate(releaseDate, BEFOREDAY)) {

                            Element aElement = element.selectFirst("a");
                            String path = aElement.attr("href");
                            if (StringUtil.isBlank(path)) continue;
                            path = path.replaceFirst("../..", "");
                            // 将所有已知的信息保存到htmlContent,包括：url, title， data， from， libtype
                            HtmlContent html = new HtmlContent(host + path, aElement.text(), null,
                                    null, releaseDate, source.getFrom(), source.getLibraryType());
                            urlQueue.add(html);
                        } else {
                            // 因为信息是按照日期排序的，最新的都在前面，一旦碰到待爬取日期外的连接，就表示之后的内容都是不需要爬取，可以直接推出循环
                            nextPage = false;
                            break;
                        }
                    }
                    // 从第二页开始，url路径会在index加数字,第二页加的是1，第三页加的是2.....
                    url = sourceUrl.replaceAll(".htm", "_" + curPage + ".htm");
                }
            }
        } catch (IOException e) {
            log.info("urls extraction completed");
        }
        log.info("---------------------[ " + urlQueue.size() +  " ] urls has been extracted from the given source");
        return urlQueue;
    }


    /**
     * 从js中抽取出总页数
     * @param doc
     * @return
     */
    public int extractPageCount(Document doc) {
        Elements elements = doc.selectFirst("div.page").getElementsByTag("script");
        String[] vars = elements.get(0).data().split("var");
        for(String variable : vars){
            /*过滤variable为空的数据*/
            if(variable.contains("=")){
                /*取到满足条件的JS变量*/
                if(variable.contains("countPage")){
                    String tempValue = variable.split("=")[1];
                    if (tempValue != null) {
                        String pageCountStr = tempValue.replaceAll("[^0-9]", "");
                        return Integer.valueOf(pageCountStr);
                    }
                }
            }
        }
        return -1;
    }


    /**
     * 抽取所有队列中url的内容，直接通过标签的特征获取
     * @param urlQueue 资源路径队列
     */
    public void collect (BlockingQueue<HtmlContent> urlQueue) {

        while (urlQueue.size() > 0) {
            HtmlContent html = urlQueue.poll();
            // 待爬取页面完整url
            try {
                log.info("++++++begin to crawl url: " + html.getUrl());
                Document doc = JsoupUtil.getDocument(html.getUrl());

                // 获得内容
                Element element = doc.selectFirst(".content");
                element.select(".title").remove();
                element.select(".time").remove();
                String content = element.text();

                // 抽取文件号，优先在正文中查找，再从标题中抽取
                String fileNum = "";
                // String regex = "〔[0-9]{4}〕[0-9]+号";
                String regex = ".[0-9]{4}.{1,2}[0-9]*.?号";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    content = content.replaceAll(matcher.group(0), "");
                    fileNum = matcher.group(0).replace("[","〔").replace("]","〕").replace(" ","");
                }
                else{
                    matcher = pattern.matcher(html.getTitle());
                    if (matcher.find()) {
                        content = content.replaceAll(matcher.group(0), "");
                        fileNum = matcher.group(0).replace("[","〔").replace("]","〕").replace(" ","");
                    }
                    else
                        fileNum = "未识别";
                }
                html.setContent(content);
                html.setFileNum(fileNum);
                // 保存到数据库
                DBUtil.save2Database(html);

                // 保存html到本地
                removeUseless(doc);
                JsoupUtil.saveHtml(doc.html(), Configuration.getProperty("html.save.path"), html);
                log.info("succeed to save html source to local");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.currentThread().sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void removeUseless(Document doc) {
        doc.select(".zi_menu").remove();
        doc.select("img").remove();
        doc.select(".topbar").remove();
        doc.select(".sobox").remove();
        doc.select("tbody").remove();
    }

}
