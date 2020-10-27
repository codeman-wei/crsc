package com.Wind.utils;

import com.Wind.domain.DBAccount;
import com.Wind.domain.DBCompany;
import com.Wind.domain.ItemInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

class DBUtilTest {

    @Test
    void getCompanyList() {
        // 测试数据连接的数据获取情况
        System.out.println("开始测试");
        List<DBCompany> companyLists = DBUtil.getCompanyList();
        for (DBCompany company : companyLists){
            System.out.println(company.getCompanyId().toString()+"   "+company.getCompanyName()+"   "+company.getNewestDocDate());
        }
    }

    @Test
    void save2Database() {
        // 测试数据库的数据插入情况
        ItemInfo result = new ItemInfo();
        Integer companyId = 21;
        result.setCorpName("corpName");
        result.setCaseName("caseName");
        result.setCaseId("caseId");
        result.setCaseReason("caseReason");
        result.setCaseAmount("caseAmount");
        result.setPlaintiff("plaintiff");
        result.setDefendant("defendant");
        result.setAgent("agent");
        result.setThirdParties("thirdParties");
        result.setJudgeResult("judgeResult");
        result.setJudgeDetail("judgeDetail");
        result.setJudgeDate("judgeDate");
        result.setProvince("province");
        result.setCourt("court");
        result.setPubDate("pubDate");
        result.setDocType("docType");
        DBUtil.save2Database(result,companyId);
    }

    @Test
    void updateNewestDocDate(){
        System.out.println("开始测试");
        DBCompany testCompany = new DBCompany(8,"百度","2020-01-01",0);
        String WindId = "123456789";
        DBUtil.updateNewestDocDate(testCompany,WindId);
    }

    @Test
    void getWindAccount(){
        System.out.println("开始测试");
        List<DBAccount> accountList = DBUtil.getWindAccount();
        if (accountList.size() == 0)
            System.out.println("不存在有效账户");
        else{
            DBAccount windaccount = accountList.get(0);
            System.out.println(windaccount.getAccount());
        }
    }
}