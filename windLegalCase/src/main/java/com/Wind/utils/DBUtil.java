package com.Wind.utils;

import com.Wind.domain.DBCompany;
import com.Wind.domain.ItemInfo;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

@Slf4j
public class DBUtil {
    /**
     * 从数据库读取企业名称列表
     * @return
     */
    public static List getCompanyList() {
        Connection con = null;
        PreparedStatement query = null;
        ResultSet result = null;
        String sql = "select id,company_name,newest_doc_date,num_of_crawl from company_list WHERE num_of_crawl = (SELECT min(num_of_crawl) FROM company_list);";
        // 创建列表用于接收数据库返回的内容
        ArrayList<DBCompany> companyList = new ArrayList<>();
        try {
            // 加载Mysql数据驱动
            Class.forName("com.mysql.jdbc.Driver");
            // 创建数据连接
            con = DriverManager.getConnection("jdbc:mysql://210.34.58.8:3306/csrc_test?useUnicode=true&characterEncoding=UTF-8", "root", "123456");
            query = con.prepareStatement(sql);
            // 执行sql语句
            result = query.executeQuery();
            while (result.next()) {
                DBCompany item = new DBCompany(result.getInt("id"),result.getString("company_name"),result.getString("newest_doc_date"),result.getInt("num_of_crawl"));
                companyList.add(item);
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
        return companyList;
    }

    /**
     * 将获取的裁判文书保存到数据库
     * @param result
     * @param companyId
     */
    public static void save2Database (ItemInfo result,Integer companyId) {
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            // 注册数据库的驱动
            Class.forName("com.mysql.jdbc.Driver");
            // 获取数据库连接
            connection = DriverManager.getConnection("jdbc:mysql://210.34.58.8:3306/csrc_test?useUnicode=true&characterEncoding=UTF-8",
                    "root", "123456");
            // 执行需要执行的语句（？是占位符号，代表一个参数）,数据库需要建立唯一联合索引实现不重复插入
            String sql ="insert ignore into wind_judge_doc(company_id,corp_name,case_name,case_id,case_reason,case_amount,plaintiff," +
                    "defendant,agent,third_parties,judge_result,judge_detail,judge_date,province,court,pub_date,doc_type) values " +
                    "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            // 获取预处理对象,并赋参
            statement = connection.prepareCall(sql);
            statement.setInt(1,companyId);
            statement.setString(2, result.getCorpName());
            statement.setString(3, result.getCaseName());
            statement.setString(4, result.getCaseId());
            statement.setString(5, result.getCaseReason());
            statement.setString(6, result.getCaseAmount());
            statement.setString(7, result.getPlaintiff());
            statement.setString(8, result.getDefendant());
            statement.setString(9, result.getAgent());
            statement.setString(10, result.getThirdParties());
            statement.setString(11, result.getJudgeResult());
            statement.setString(12, result.getJudgeDetail());
            statement.setString(13, result.getJudgeDate());
            statement.setString(14, result.getProvince());
            statement.setString(15, result.getCourt());
            statement.setString(16, result.getPubDate());
            statement.setString(17, result.getDocType());
            // 执行sql语句
            statement.executeUpdate();
            // log.info("Insert result to database: " + result.toString());
        }catch (Exception e) {
            // e.printStackTrace();
            log.error("记录"+result.toString()+"插入数据库失败");
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
     * 更新公司的最新文书时间以及爬取次数
     * @param company
     */
    public static void updateNewestDocDate (DBCompany company,String WindId) {
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            // 注册数据库的驱动
            Class.forName("com.mysql.jdbc.Driver");
            // 获取数据库连接
            connection = DriverManager.getConnection("jdbc:mysql://210.34.58.8:3306/csrc_test?useUnicode=true&characterEncoding=UTF-8", "root", "123456");
            // 执行需要执行的语句（？是占位符号，代表一个参数）
            String sql = "update company_list set wind_id = ?, newest_doc_date = ?, num_of_crawl = ? where id = ?";
            // 获取预处理对象,并赋参
            statement = connection.prepareCall(sql);
            statement.setString(1,WindId);
            statement.setString(2,company.getNewestDocDate());
            statement.setInt(3,company.getNumOfCrawl());
            statement.setInt(4,company.getCompanyId());
            // 执行sql语句
            statement.executeUpdate();
            log.info("更新公司列表，更新内容:" + company.toString());
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
     * 更新新增公司的爬取次数
     */
    public static void updateCrawlNum () {
        Connection connection = null;
        PreparedStatement statement = null;
        try{
            // 注册数据库的驱动
            Class.forName("com.mysql.jdbc.Driver");
            // 获取数据库连接
            connection = DriverManager.getConnection("jdbc:mysql://210.34.58.8:3306/csrc_test?useUnicode=true&characterEncoding=UTF-8", "root", "123456");
            // 执行需要执行的语句
            String sql = "update company_list set num_of_crawl = (SELECT a.maxnum FROM (SELECT MAX(num_of_crawl)-1 maxnum FROM company_list) a) where num_of_crawl = (SELECT b.minnum FROM (SELECT MIN(num_of_crawl) minnum FROM company_list) b);";
            // 获取预处理对象,并赋参
            statement = connection.prepareCall(sql);
            // 执行sql语句
            statement.executeUpdate();
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
