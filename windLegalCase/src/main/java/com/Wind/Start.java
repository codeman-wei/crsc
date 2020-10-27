package com.Wind;

import com.Wind.domain.DBAccount;
import com.Wind.service.GetWinData;
import com.Wind.utils.DBUtil;

import java.util.List;

public class Start {
    public static void main(String[] args) throws Exception {
        // 更新新增公司的爬取次数
        DBUtil.updateCrawlNum();
        // 检索
        GetWinData.getWindData();
    }
}