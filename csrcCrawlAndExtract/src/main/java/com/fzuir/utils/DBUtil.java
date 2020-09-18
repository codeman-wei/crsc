package com.fzuir.utils;

import com.fzuir.domain.HtmlContent;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DBUtil {
    /**
     * 从数据库读url信息
     * @return
     */
    public static List getSource() {
        Connection con = null;
        PreparedStatement query = null;
        ResultSet result = null;
        String sql = "select url,library_type,source from origin_url";
        List sources = new ArrayList(); // 创建列表用于接收数据库返回的内容
        try {
            Class.forName("com.mysql.jdbc.Driver");// 加载Mysql数据驱动
            con = DriverManager.getConnection("jdbc:mysql://210.34.58.8:3306/csrc_test?useUnicode=true&characterEncoding=UTF-8", "root", "123456");// 创建数据连接
            query = con.prepareStatement(sql);
            result = query.executeQuery();
            int num = result.getMetaData().getColumnCount();// getMetaData获得表结构，getColunmCount获得字段数
            while (result.next()) {
                Map mapOfColValues = new HashMap(num);
                for (int i = 1; i<= num; i++) {
                    mapOfColValues.put(result.getMetaData().getColumnName(i),result.getObject(i)); // 获取字段名及其对应内容
                }
                sources.add(mapOfColValues);
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

    public static void save2Database (HtmlContent result) {
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            // 注册数据库的驱动
            Class.forName("com.mysql.jdbc.Driver");
            // 获取数据库连接
            connection = DriverManager.getConnection("jdbc:mysql://210.34.58.8:3306/csrc_test?useUnicode=true&characterEncoding=UTF-8", "root", "123456");
            // 执行需要执行的语句（？是占位符号，代表一个参数）
            String sql = "insert into legal_cases(title,document_number,content,pub_date,source,url,library_type) values (?,?,?,?,?,?,?)";
            // 获取预处理对象,并赋参
            statement = connection.prepareCall(sql);
            statement.setString(1, result.getTitle());
            statement.setString(2, result.getFileNum());
            statement.setString(3, result.getContent());
            statement.setString(4, result.getDate());
            statement.setString(5, result.getFrom());
            statement.setString(6, result.getUrl());
            statement.setString(7, result.getLibtype());
            // 执行sql语句
            statement.executeUpdate();
            log.info("add result to database: " + result.toString());
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                // 关闭连接
                statement.close();
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    /**
     * 将数据保存为HTML
     * @param html
     * @param url
     * @param basePath
     */
    public static void saveHtml(String html, String url, String basePath) throws UnsupportedEncodingException {
        String encode = getEncode(html);
        SimpleDateFormat dayformat = new SimpleDateFormat("yyyyMMdd");
        dayformat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String date = dayformat.format(System.currentTimeMillis());
        String mkdirPath = basePath + "/" + date;
        checkPath(mkdirPath);
        String filepath = mkdirPath+"/"+getTimestamp() + "_" + getRandomString() + "_" + getHost(url) + ".html";
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
     * 获取主机名
     * 参数1：url为输入的url全名
     * */
    public static String getHost(String url){
        String startUrl = url;

        startUrl = startUrl.replace("http://", "");
        startUrl = startUrl.replace("https://", "");
        startUrl = startUrl.replace('/', '.');
        int indexOfSlash = startUrl.indexOf("/");
//        if(indexOfSlash != -1)
//        {
//            startUrl = startUrl.substring(0,indexOfSlash);
//        }
//        int indexOfPoint = startUrl.indexOf(".");
//        if(indexOfPoint != -1)
//        {
//            startUrl = startUrl.substring(indexOfPoint+1,startUrl.length());
//        }
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
