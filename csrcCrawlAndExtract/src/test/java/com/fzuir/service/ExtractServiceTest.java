package com.fzuir.service;

import com.fzuir.domain.HtmlContent;
import com.fzuir.domain.Source;
import com.fzuir.utils.DateUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class ExtractServiceTest {
    ExtractService extractor = new ExtractService();

    @Test
    void getPathQueue() {
        BlockingQueue<HtmlContent> test = extractor.getPathQueue(new Source("http://www.csrc.gov.cn/pub/beijing/bjxyzl/bjxzcf/", "福建局", "行政处罚"));
        System.out.println(test.size());

    }

    @Test
    void collect() {
        BlockingQueue<HtmlContent> test = extractor.getPathQueue(new Source("http://www.csrc.gov.cn/pub/zjhpublic/3300/3313/index_7401_1.htm", "福建局", "行政处罚"));
        System.out.println(test.size());
        extractor.collect(test);
    }
}