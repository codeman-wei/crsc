package com.fzuir.utils;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

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
}
