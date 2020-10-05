package com.Wind.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WindUtilTest {

    @Test
    void compareDate() {
        String str1 = "";
        String str2 = "2019-01-01";
        if (WindUtil.compareDate(str1,str2))
            System.out.println(str1);
        else
            System.out.println(str2);
        int pageNum = 1;
        int pageSize = 6;
        int Sum = (pageNum-1)*pageSize;
        pageSize = pageSize/2;
        pageNum = Sum/pageSize;
        System.out.println(pageSize+"/"+pageNum);
    }
}