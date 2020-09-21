package com.fzuir.utils;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    void checkDate() {
        Assert.assertTrue(DateUtil.checkDate("2020年9月18日", 3));
    }
}