public class test {
    public static void main(String[] args) {
        test test = new test();
        System.out.println(test.getHost("http://www.csrc.gov.cn/pub/hunan/hnxzcf/202008/t20200826_382169.htm"));
    }
    public String getHost(String url){
        String startUrl = url;
        startUrl = startUrl.replace("http://", "");
        startUrl = startUrl.replace("https://", "");
        int index= startUrl.indexOf(".htm");
        if(index != -1)
        {
            startUrl = startUrl.substring(0, index);
        }
        return startUrl;
    }
}
