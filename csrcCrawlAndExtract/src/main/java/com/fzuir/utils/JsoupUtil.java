package com.fzuir.utils;

import com.fzuir.domain.HtmlContent;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class JsoupUtil {
    public static Document getDocument(String url){
        int maxRetry = 3;   // 最大重试次数
        int sleepTime = 4;  // 连接失败再尝试前休息几秒
        final int TIMEOUT = 10;


        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Document doc = null;
        for (int i = 1; i <= maxRetry; i++) {
            try {
                if (i != 1) {
                    Thread.sleep(sleepTime * 1000);
                }
                doc = Jsoup.connect(url)
                        .timeout(TIMEOUT * 1000)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36")
                        .get();
                // 连接成功就退出
                break;
            } catch (Exception e) {
                e.printStackTrace(printWriter);
                String stringTrace = printWriter.toString();
                log.info(stringTrace);
            } finally {
                if (stringWriter != null) {
                    try {
                        stringWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (printWriter != null) {
                    printWriter.close();
                }
            }

        }
        return doc;
    }


    /**
     *
     * @param html
     * @param basePath
     * @param info
     */
    public static void saveHtml(String html, String basePath, HtmlContent info)  {

        String mkdirPath = basePath + "/" + info.getLibtype() + '/' + info.getFrom();
        checkPath(mkdirPath);
        String filepath = mkdirPath+"/"+getTimestamp() + "_" + getRandomString() + "_" + info.getTitle() + ".html";

        // 通过html内容获取其编码
        String encode = getEncode(html);
        // 输出流保存文件
        byte[] bs = new byte[1024];
        int len;
        OutputStream os = null;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(html.getBytes(encode));
            os = new FileOutputStream(filepath);
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (os != null) os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



    /**
     * 通过查找charset属性找到html的编码
     * @param html  待查找编码的html内容
     * @return
     */
    public static String getEncode(String html) {

        String charset = "";
        String regEx = "(charset=(\")?[a-zA-Z0-9-]*)";

        Pattern pattern=Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            charset = matcher.group(0).replaceAll("\"", "");
            String[] splits = charset.split("=");
            if (splits.length > 1) charset = splits[1];
        }
        if(charset == "" || charset == null || charset.equals(""))
        {
            return "gbk";
        } else {
            return charset;
        }
    }

    // 检查路径的文件夹是否都存在
    public static void checkPath(String path) {
        File file = new File(path);
        if(!file.exists() && !file .isDirectory())
        {
            file.mkdirs();
        }
    }

    // 获取时间戳
    public static String getTimestamp(){
        return Long.toString(System.currentTimeMillis());
    }


    // 获得五位随机数
    private static String getRandomString() {
        String s = "";
        for(int i=0; i<5; ++i) {
            s += (int)(Math.random()*10);
        }
        return s;
    }
}
