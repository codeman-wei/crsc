package com.Wind.utils;

import com.Wind.domain.DBCompany;
import com.Wind.domain.ItemInfo;
import com.Wind.domain.ResponseEntity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log4j
public class WindUtil {

    private static final String BASE_URL = "http://eapi.wind.com.cn/wind.ent.risk/openapi";

    public static final byte[] getMd5Digest(String s) {
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            java.security.MessageDigest mdInst = java.security.MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            return md;
        } catch (Exception e) {
            throw new RuntimeException("加密失败");
        }
    }

    public static String digestString(String string) {
        byte[] digest = WindUtil.getMd5Digest(string);
        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(digest);
    }


    public static boolean compareDate(String str1, String str2) {
        // 创建日期转换年月日为yyyy-MM-dd
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // 将字符串转换为date类型
            Date dt1 = df.parse(str1);
            Date dt2 = df.parse(str2);
            // 比较时间大小
            if (dt1.getTime() > dt2.getTime())
            {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            log.error("日期对比存在异常，异常值为:"+str1);
            // e.printStackTrace();
        }
        return true; // 未知大小按照新数据处理
    }

    /**
     * 获取token
     * 请务必注意：不要每次调用接口都重新取得token，
     * 太多调用会被认为账户异常，最终影响wft账户的正常使用
     */
    public static String fetchToken(CloseableHttpClient httpClient, String user, String verifyCode) throws Exception {
        String url = new StringBuilder(BASE_URL)
                .append("/getToken?")
                .append("windUser=")
                .append(user)
                .append("&")
                .append("verifyCode=")
                .append(verifyCode)
                .toString();
        HttpGet getReuqest = new HttpGet(url);
        CloseableHttpResponse response = null;
        String token = null;
        try {
            response = httpClient.execute(getReuqest);
            String resultJson = EntityUtils.toString(response.getEntity(), "UTF-8");
            ResponseEntity responseEntity = JSON.parseObject(resultJson, ResponseEntity.class);
            if (responseEntity.getErrorCode() == 0) {
                JSONObject data = (JSONObject) responseEntity.getSource();
                token = String.valueOf(data.get("token"));
                if (token.equals("")){
                    log.error("您的账号可能没有获得token的权限");
                    throw new RuntimeException("获取token失败");
                }
            } else{
                log.error("获取token的异常为:"+responseEntity.getMessage());
                throw new RuntimeException("获取token失败");
            }
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return token;
    }

    /***
     * 如果errorCode = 403时，
     * 则说明token过期，或错误，
     * 需要通过上面接口重新取得token
     *
     */
    public static String fetchWindId(CloseableHttpClient httpClient, String token, String corpName) throws Exception {
        String url = new StringBuilder(BASE_URL)
                .append("/searchcorplist?")
                .append("token=")
                .append(token)
                .append("&")
                .append("corpName=")
                .append(corpName)
                .toString();
        HttpGet getReuqest = new HttpGet(url);
        CloseableHttpResponse response = null;
        String windId = null;
        try {
            response = httpClient.execute(getReuqest);
            String resultJson = EntityUtils.toString(response.getEntity(), "UTF-8");
            ResponseEntity responseEntity = JSON.parseObject(resultJson, ResponseEntity.class);
            if (responseEntity.getErrorCode() == 0) {
                JSONObject searchResult = (JSONObject) responseEntity.getSource();
                JSONArray jsonArray = (JSONArray) searchResult.get("items");
                if (jsonArray.isEmpty()) {
                    windId = null;
                } else {
                    JSONObject item = (JSONObject) jsonArray.get(0);
                    windId = String.valueOf(item.get("windId"));
                }
            } else if (responseEntity.getErrorCode() == 403) {
                windId = "ErrorCode=403";
                log.warn("获取windId失败,错误编号:"+responseEntity.getErrorCode()+"，说明:"+responseEntity.getMessage());
            } else {
                log.error("获取windId失败,错误编号:"+responseEntity.getErrorCode()+"，说明:"+responseEntity.getMessage());
            }
        }  catch (Exception e){
            log.error("公司"+corpName+"http请求异常[WindID]，详情为:"+e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                    log.error(e);
                }
            }
        }
        return windId;
    }

    /**
     * 通过windId取相关类型数据，
     * 具体参数参看每个接口的输入参数
     * 如果errorCode = 403时，
     * 则说明token过期，或错误，
     * 需要通过上面接口重新取得token
     */
    public static int
    queryCorpInfo(CloseableHttpClient httpClient, String apiId, String windId, String token, int pageNum, int pageSize, DBCompany company,String newestDate) throws Exception {
        String url = new StringBuilder(BASE_URL)
                .append("/corpinfo/").append(apiId)
                .append("?token=")
                .append(token)
                .append("&")
                .append("windId=")
                .append(windId)
                .append("&").append("pageIndex=").append(pageNum).append("&pageSize=").append(pageSize)
                .toString();
        HttpGet getReuqest = new HttpGet(url);
        CloseableHttpResponse response = null;
        Integer total = 0;
        try {
            response = httpClient.execute(getReuqest);
            // 将获取的内容转化为String
            String resultJson = EntityUtils.toString(response.getEntity(), "UTF-8");
            // System.out.println("获得原生内容：\n"+resultJson);
            // 解析返回对象
            ResponseEntity responseEntity = JSON.parseObject(resultJson, ResponseEntity.class);
            // 判断请求是否出错
            if (responseEntity.getErrorCode() == 0) {
                // 未出错，抽取出核心数据
                JSONObject searchResult = (JSONObject) responseEntity.getSource();
                // 提取总条数
                total = (Integer) searchResult.get("total");
                // 构建记录的JSONArray数组
                JSONArray items = (JSONArray) searchResult.get("items");
                // 若不存在记录则仅更新编号
                if (!items.isEmpty()){
                    for (Object item : items) {
                        ItemInfo value = JSON.parseObject(item.toString(), ItemInfo.class);
                        String newdate = value.getJudgeDate();
                        if (newdate.equals("") || newdate == null) {
                            log.warn("记录["+value.getCaseName()+"|"+value.getCaseId()+"]的裁判时间为空值,处理:直接插入");
                            // 未知时间为保证数据的完整性，直接插入数据库
                            DBUtil.save2Database(value, company.getCompanyId());
                        } else{
                            if (compareDate(newdate,newestDate)){
                                // 将所有的新数据保存到数据库
                                DBUtil.save2Database(value, company.getCompanyId());
                                // 查找第一页的最新时间即为最新时间
                                if (pageNum == 1 && compareDate(newdate,company.getNewestDocDate())){
                                    company.setNewestDocDate(newdate);
                                }
                            }
                            else
                                return 0;
                        }
                    }
                }
            }else if (responseEntity.getErrorCode() == 300001 && responseEntity.getMessage().equals("judgmentDoc失败！")){
                // 如果步长减为1则无需继续
                if (pageSize == 1){
                    log.error("企业:"+company.getCompanyName()+"第"+ pageNum + "条及以后内容不能正常返回,错误代码:" + responseEntity.getErrorCode() + ",错误信息:" + responseEntity.getMessage());
                    return 0;
                } else {
                    return 300001;
                }
            }else{
                return responseEntity.getErrorCode();
            }
        } catch (Exception e){
            log.error("公司"+company.getCompanyName()+"http请求异常[内容]，详情为:"+e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 判断是否存在下一页内容
        if ((total - pageNum * pageSize) > 0)
            return 1; // 存在
        else
            return 0; // 不存在
    }

    /**
     * 通过windId取相关类型数据，
     * 具体参数参看每个接口的输入参数
     * 核查类接口调用此接口
     *
     */
    private static void verifyCorpInfo(CloseableHttpClient httpClient, String apiId, String options, String windId, String token) throws Exception {
        String url = new StringBuilder(BASE_URL)
                .append("/verifyCorpInfo/").append(apiId)
                .append("?token=")
                .append(token)
                .append("&")
                .append("windId=")
                .append(windId)
                .append("&")
                .append(options)
                .toString();
        // 构建请求方法的实例HttpGet\HttpPost
        HttpGet getReuqest = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(getReuqest);
            String resultJson = EntityUtils.toString(response.getEntity(), "UTF-8");
            ResponseEntity responseEntity = JSON.parseObject(resultJson, ResponseEntity.class);
            if (responseEntity.getErrorCode() == 0) {
                System.out.println(JSON.toJSON(responseEntity.getSource()));
            } else {
                log.error("获取企业失败," + responseEntity.getErrorCode() + "," + responseEntity.getMessage());
            }
        }catch (Exception e){
           log.error("http请求异常");
        }  finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
