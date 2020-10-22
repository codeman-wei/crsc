package com.Wind;

import com.Wind.service.GetWinData;
import com.Wind.utils.DBUtil;

public class Start {
    public static void main(String[] args) throws Exception {
        // 确定用户以及密码
        String user = "W8750909462";
        // String user = "15259093250"; // 测试账户
        String pwd = "Fzuir070399";
        // 确定获取资料的类型，"D002"为裁判文书
        String apiId = "D002";
        // 确定请求链接
        String base_url = "http://eapi.wind.com.cn/wind.ent.risk/openapi";
        // 检索
        GetWinData.getWindData(user, pwd, apiId, base_url);
        // 更新新增公司的爬取次数
        DBUtil.updateCrawlNum();
    }
}