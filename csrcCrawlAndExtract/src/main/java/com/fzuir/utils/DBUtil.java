package com.fzuir.utils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBUtil {

    /**
     * 从数据库读url信息
     * @return
     */
    public static Set<String> getSource() {
        Connection con = null;
        PreparedStatement query = null;
        ResultSet result = null;
        String sql = "select url from origin_url";
        Set<String> sources = new HashSet<String>();
        try {
            Class.forName("com.mysql.jdbc.Driver");// 加载Mysql数据驱动
            con = DriverManager.getConnection("jdbc:mysql://210.34.58.8:3306/csrc_test?useUnicode=true&characterEncoding=UTF-8", "root", "123456");// 创建数据连接
            query = con.prepareStatement(sql);
            result = query.executeQuery();
            while (result.next()) {
                String url = result.getString(1);
                if (url.charAt(url.length() - 1) == '/' || url.charAt(url.length() - 1) == '\\') {
                    url = url.substring(0,url.length() - 1);
                }
                sources.add(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (result != null) result.close();
                if (query != null) query.close();
                if (con != null) con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return sources;
    }


    public static void saveHtml(String html, String url, String basePath) {
        String encode = getEncode(html);
        SimpleDateFormat dayformat = new SimpleDateFormat("yyyyMMdd");
        dayformat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String date = dayformat.format(System.currentTimeMillis());
        String mkdirPath = basePath + "/" + date;
        checkPath(mkdirPath);
        String filepath = mkdirPath+"/"+getTimestamp() + "_" + getRandomString() + "_" + getHost(url) + ".html";
        // 输出流保存文件
        FileOutputStream fout = null;
        FileChannel fc = null;
        FileLock fl = null;
        try {
            fout = new FileOutputStream(filepath);
            fc = fout.getChannel();
            fl = fc.tryLock();
            OutputStreamWriter out = new OutputStreamWriter(fout, encode);
            out.write(url+System.getProperty("line.separator"));
            out.write(html);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fl != null) fl.close();
                if (fc != null) fc.close();
                if (fout != null) fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 获取主机名
     * 参数1：url为输入的url全名
     * */
    public static String getHost(String url){
        String startUrl = url;
        startUrl = startUrl.replace("http://", "");
        startUrl = startUrl.replace("https://", "");
        int indexOfSlash = startUrl.indexOf("/");
        if(indexOfSlash != -1)
        {
            startUrl = startUrl.substring(0,indexOfSlash);
        }
        int indexOfPoint = startUrl.indexOf(".");
        if(indexOfPoint != -1)
        {
            startUrl = startUrl.substring(indexOfPoint+1,startUrl.length());
        }
        return startUrl;
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
