package com.Wind.service;

import com.Wind.domain.DBAccount;
import com.Wind.domain.DBCompany;
import com.Wind.utils.DBUtil;
import com.Wind.utils.WindUtil;
import lombok.extern.log4j.Log4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Log4j
public class GetWinData {
    /**
     * 读取数据库公司列表，获取Wind的裁判文书
     * @throws Exception
     */
    public static void getWindData() throws Exception {
        String user = "W8750909462";
        String pwd = "Fzuir070399";
        String apiId = "D002";
        String base_url = "http://eapi.wind.com.cn/wind.ent.risk/openapi";
        // 初始化账号
        List<DBAccount> accountList = DBUtil.getWindAccount();
        if (accountList.size() == 0){
            log.error("不存在有效账户");
            System.out.println("不存在有效账户");
        }
        else {
            DBAccount windaccount = accountList.get(0);
            // 确定用户以及密码
            user = windaccount.getAccount(); // 测试账户
            pwd = windaccount.getPassword(); // 测试账户密码
            apiId = windaccount.getDocType(); // 确定获取资料的类型，"D002"为裁判文书
            base_url = windaccount.getPostUrl(); // 确定请求链接
        }
        // 初始化列表公司名称
        List<DBCompany> companyLists = DBUtil.getCompanyList();
        // 创建连接
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 将密码加密生成识别码（MD5）
        String verifyCode = WindUtil.digestString(pwd);
        // 通过用户名和识别码得到token，调用其他接口时如果errorCode = 403 时，可以尝试再次取得token
        String token = WindUtil.fetchToken(httpClient, user, verifyCode, base_url);
        // 定义展示的格式:pageIndex显示第几页,pageSize每页显示的条数
        int pageNum = 1;
        int pageSize = 50;
        for (DBCompany company : companyLists){
            // 保存数据库中记录的最新时间
            String newestdate = company.getNewestDocDate();
            // 通过token调取搜索接口得到windId
            String windId = WindUtil.fetchWindId(httpClient, token, company.getCompanyName(), base_url);
            if (windId == null) {
                log.info("公司"+company.getCompanyName()+"的WindId不存在,跳过");
                // 加浏览次数
                company.setNumOfCrawl(company.getNumOfCrawl()+1);
                DBUtil.updateNewestDocDate(company, null);
                continue;
            }
            //判断是否存在下一页,默认存在(1存在，不存在，403重新获取token)
            int nextFlag = 1;
            // 由windId查询相关接口
            while (nextFlag > 0) {
                // 获取每一页数据前休息5s~10s
                Random rand = new Random();
                try {
                    Thread.sleep((rand.nextInt(5) + 5)*1000);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                    log.warn("程序休眠异常");
                }
                // token失效处理
                if (windId.equals("ErrorCode=403")) {
                    // 获取的token过期，应重新获取
                    token = WindUtil.fetchToken(httpClient, user, verifyCode, base_url);
                    windId = WindUtil.fetchWindId(httpClient, token, company.getCompanyName(), base_url);
                    if (windId == null) {
                        log.info("公司:"+company.getCompanyName()+"的WindId不存在,处理:跳过");
                        break;
                    } else
                        continue;
                }
                // 获取编号为winId公司的第pageNum页内容,并判断是否存在下一页
                nextFlag = WindUtil.queryCorpInfo(httpClient, apiId, windId, token, pageNum, pageSize, company, newestdate, base_url);
                switch (nextFlag){
                    case 403:
                        // 获取的token过期，应重新获取
                        token = WindUtil.fetchToken(httpClient, user, verifyCode, base_url);
                        continue;
                    case 300001:
                        log.warn("获取企业:"+company.getCompanyName()+"第"+ pageNum + "页内容失败("+ pageSize +"条/页),错误代码:" + 300001 + ",错误信息:请求失败");
                        // 存在异常页不能获取，减半pageSize
                        int Sum = (pageNum-1)*pageSize;
                        pageSize = pageSize/2;
                        pageNum = Sum/pageSize;
                        break;
                    case 300005:
                        log.error("获取企业:"+company.getCompanyName()+"第"+ pageNum + "页内容失败("+ pageSize +"条/页),错误代码:" + 300005 + ",错误信息:无权限（比如没有终端账号）");
                        nextFlag = 0;
                        break;
                    case 300006:
                        log.error("获取企业:"+company.getCompanyName()+"第"+ pageNum + "页内容失败("+ pageSize +"条/页),错误代码:" + 300006 + ",错误信息:超过规定时间段调用次数（周、月、年）");
                        nextFlag = 0;
                        break;
                    case 300007:
                        log.error("获取企业:"+company.getCompanyName()+"第"+ pageNum + "页内容失败("+ pageSize +"条/页),错误代码:" + 300007 + ",错误信息:剩余次数不足");
                        nextFlag = 0;
                        break;
                    case 300008:
                        log.error("获取企业:"+company.getCompanyName()+"第"+ pageNum + "页内容失败("+ pageSize +"条/页),错误代码:" + 300008 + ",错误信息:缺少必要的参数");
                        nextFlag = 0;
                        break;
                    case 300009:
                        log.error("获取企业:"+company.getCompanyName()+"第"+ pageNum + "页内容失败("+ pageSize +"条/页),错误代码:" + 300009 + ",错误信息:参数输入不正确（比如输入一个不存在的BizType）");
                        nextFlag = 0;
                        break;
                }
                pageNum++;
            }
            // 更新文书最新时间以及爬取次数
            company.setNumOfCrawl(company.getNumOfCrawl()+1);
            DBUtil.updateNewestDocDate(company,windId);
            // 初始化页数
            pageNum = 1;
            pageSize = 50;
        }
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
