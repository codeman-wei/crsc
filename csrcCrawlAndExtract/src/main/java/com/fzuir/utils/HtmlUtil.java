package com.fzuir.utils;

import com.fzuir.domain.HtmlContent;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.StringUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class HtmlUtil {

    /**
     *
     * @param html
     * @param basePath
     * @param info
     */
    public static void saveHtml(String html, String basePath, HtmlContent info)  {

//        SimpleDateFormat dayformat = new SimpleDateFormat("yyyyMMdd");
//        dayformat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
//        String date = dayformat.format(System.currentTimeMillis());
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
