package com.fzuir.service;

import com.fzuir.domain.HtmlContent;
import com.fzuir.domain.Source;
import com.fzuir.utils.Configuration;
import com.fzuir.utils.DBUtil;
import com.fzuir.utils.DateUtil;
import com.fzuir.utils.HtmlUtil;
import lombok.extern.log4j.Log4j;
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

@Log4j
public class ExtractService {
    private final static String USERAGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36";

    public void start() {
        List<Source> sources = DBUtil.getSource();
        for (Source source : sources) {
            // 通过key值获取value
            BlockingQueue<HtmlContent> pathQueue = getPathQueue(source);
            collect(pathQueue);
        }
    }

    /**
     * 给定一个来源页，从他的选项链接中抽取真正要爬取页面的url,在获得url同时能够得到标题和时间
     * @param source 来源
     * @return 将url和其他信息包装成HtmlContent对象返回
     */
    public BlockingQueue<HtmlContent> getPathQueue(final Source source) {
        log.info("---------------------begin to extract urls from source: {" + source.getUrl() + " }");
        final int BEFOREDAY = Integer.valueOf(Configuration.getProperty("extract.before.days"));
        int curPage = 0;
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
     * 抽取所有队列中url的内容，直接通过标签的特征获取
     * @param urlQueue 资源路径队列
     */
    public void collect (BlockingQueue<HtmlContent> urlQueue) {

        while (urlQueue.size() > 0) {
            HtmlContent html = urlQueue.poll();
            // 待爬取页面完整url
            try {
                log.info("++++++begin to crawl url: " + html.getUrl());
                Document doc = Jsoup.connect(html.getUrl())
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36")
                        .get();
                /*
                // 获得标题
                Element titleElement = doc.selectFirst("div.title");
                String title = titleElement.text();

                // 获得发布时间
                Element dateElement = doc.selectFirst("div.time");
                String date = dateElement.text();
                String regexd = "[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}";
                Pattern patternd = Pattern.compile(regexd);
                Matcher matcherd = patternd.matcher(date);
                if (matcherd.find()) {
                    date = matcherd.group(0);
                }
                */

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
                HtmlUtil.saveHtml(doc.html(), Configuration.getProperty("html.save.path"), html);
                log.info("succeed to save html source to local");
            } catch (Exception e) {
                e.printStackTrace();
            }
//            try {
//                Thread.currentThread().sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
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
