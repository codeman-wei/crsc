package com.fzuir.service;

import com.fzuir.utils.DBUtil;
import com.fzuir.utils.DateUtil;
import lombok.extern.log4j.Log4j;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j
public class ExtractService {
    final static String htmlSavePath = "E:\\sts\\csrc\\cfjds";
    final static String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36";
    final static int beforeDays = -1;

    public void start() {
        log.info("从数据读取待采集的url来源:");
        // 从数据读取待采集的url来源
        Set<String> sources = DBUtil.getSource();
        for (String source : sources) {
            System.out.println("*******************************************************");
            BlockingQueue<String> pathQueue = getPathQueue(source);
            collect(source, pathQueue);
        }
    }

    /**
     * 给定一个来源页，从他的选项链接中抽取真正要爬取页面的url
     * @param url 来源页
     * @return 该来源页所有需要爬取的页面url
     */
    public BlockingQueue<String> getPathQueue(String url) {
        int curPage = 1;
        boolean nextPage = true;
        BlockingQueue<String> pathQueue = new LinkedBlockingDeque<>(100);
        Document doc = null;
        try {
            while (nextPage) {
                if (curPage++ != 1) {
                    // 从第二页开始，url路径会在index加数字
                    url = url + "index_" + curPage +".html";
                }
                doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .get();
                Elements elements = doc.select("div.fl_list > ul > li");
                for (Element element : elements) {
                    String releaseDate = element.selectFirst("span").text();
                    if (DateUtil.checkDate(releaseDate, beforeDays)) {
                        Element temp = element.selectFirst("a");
                        String path = temp != null ? temp.attr("href") : "";
                        if (StringUtil.isBlank(path)) continue;
                        if (path.charAt(0) == '.') {
                            path = path.replaceFirst(".", "");
                        }
                        pathQueue.add(path);
                    } else {
                        // 因为信息是按照日期排序的，最新的都在前面，一旦碰到待爬取日期外的连接，就表示之后的内容都是不需要爬取，可以直接推出循环
                        nextPage = false;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("全部页面爬取完成");
        }
        return pathQueue;
    }

    /**
     * 抽取所有队列中url的内容，直接通过标签的特征获取
     * @param pathQueue 资源路径队列
     */
    public void collect (String baseUrl,BlockingQueue<String> pathQueue) {

        while (pathQueue.size() > 0) {
            String path = pathQueue.poll();
            if (StringUtil.isBlank(path)) continue;
            // 待爬取页面完整url
            String url = baseUrl + path;
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36")
                        .get();

                // 保存html到本地
                removeUseless(doc);
                DBUtil.saveHtml(doc.html(), url, htmlSavePath);
                // 获得标题
                Element titleElement = doc.selectFirst("div.title");
                String title = titleElement.text();
                // 获得内容
                Element element = doc.selectFirst(".content");
                element.select(".title").remove();
                element.select(".time").remove();
                String content = element.text();
                // 抽取文件号
                String fileNum = "";
                String regex = "〔[0-9]{4}〕[0-9]+号";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    fileNum = matcher.group(0);
                }
                System.out.println("文件号：" + fileNum);
                System.out.println(title);
                System.out.println(content);
                System.out.println("---------------------------------------------------------");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeUseless(Document doc) {
        doc.select(".zi_menu").remove();
        doc.select("img").remove();
        doc.select(".topbar").remove();
        doc.select(".sobox").remove();
    }

}
