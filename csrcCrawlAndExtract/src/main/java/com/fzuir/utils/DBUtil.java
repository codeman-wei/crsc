package com.fzuir.utils;

import com.fzuir.domain.HtmlContent;
import com.fzuir.domain.PostData;
import com.fzuir.domain.Source;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j
public class DBUtil {
    //    private final static String database = Configuration.getProperty("database.name");
//    private final static String username = Configuration.getProperty("database.username");
//    private final static String password = Configuration.getProperty("database.password");
    private final static String database = "csrc_test";
    private final static String username = "root";
    private final static String password = "123456";


    /**
     * 从数据库读url信息
     * @return
     */
    public static List<Source> getSource() {
        log.info("**************begin to read sources from database");
        Connection con = null;
        PreparedStatement query = null;
        ResultSet result = null;
        String sql = "select url,library_type,source from origin_url";
        List<Source> sources = new ArrayList<>(40);
        try {
            Class.forName("com.mysql.jdbc.Driver");// 加载Mysql数据驱动
            con = DriverManager.getConnection("jdbc:mysql://210.34.58.8:3306/" + database + "?useUnicode=true&characterEncoding=UTF-8", username, password);// 创建数据连接
            query = con.prepareStatement(sql);
            result = query.executeQuery();

            while (result.next()) {
                sources.add(new Source(result.getString(1), result.getString(2), result.getString(3)));
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
        log.info("***********Get source completed, there are [ " + sources.size() + " ] sources need to be extracted");
        return sources;
    }


    /**
     * 从数据库读url信息
     * @return
     */
    public static List<PostData> getPostData() {
        log.info("**************begin to read postData from database");
        Connection con = null;
        PreparedStatement query = null;
        ResultSet result = null;
        String sql = "select schword, searchword, source, libary_type from post_data";
        List<PostData> postDataList = new ArrayList<>(10);
        try {
            Class.forName("com.mysql.jdbc.Driver");// 加载Mysql数据驱动
            con = DriverManager.getConnection("jdbc:mysql://210.34.58.8:3306/" + database + "?useUnicode=true&characterEncoding=UTF-8", username, password);// 创建数据连接
            query = con.prepareStatement(sql);
            result = query.executeQuery();

            while (result.next()) {
                postDataList.add(new PostData(result.getString(1), result.getString(2), result.getString(3), result.getString(4)));
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
        log.info("***********Get postData completed, there are [ " + postDataList.size() + " ] post need to be try");
        return postDataList;
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







}
