package com.Wind.utils;

import com.Wind.domain.ItemInfo;
import com.Wind.domain.ResponseEntity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class ExtractTest {
    @Test
    void testFastJson() throws IOException {
        String jsonFilePath = "C:/Users/林零零/OneDrive/实验室/项目/证监局/Wind/js.json";
        File file = new File(jsonFilePath);
        String str = FileUtils.readFileToString(file, "UTF-8");
        ResponseEntity responseEntity = JSON.parseObject(str, ResponseEntity.class);
        // 判断请求是否出错
        if (responseEntity.getErrorCode() == 0) {
            // 抽取出核心数据
            JSONObject searchResult = (JSONObject) responseEntity.getSource();
            // 提取总条数
            Integer total = (Integer) searchResult.get("total");
            Integer companyId = 1;
            // 构建记录的JSONArray数组
            JSONArray items = (JSONArray) searchResult.get("items");
            for (Object item : items) {
                ItemInfo value = JSON.parseObject(item.toString(), ItemInfo.class);
                DBUtil.save2Database(value, companyId);
            }



        }
    }

    @Test
    void testCalculateSaddN(){
        int pageNum = 1;
        int pageSize = 50;
        String source = "";
        String source0 = "Unparseable date: \"" + source + "\"";
        String options = "pageIndex="+pageNum+"&pageSize="+pageSize;
        System.out.println(options);
        System.out.println(source0);
    }
}
