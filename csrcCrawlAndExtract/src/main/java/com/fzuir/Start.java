package com.fzuir;

import com.fzuir.service.ExtractService;
import com.fzuir.utils.Configuration;

import java.io.InputStream;
import java.util.Properties;


public class Start {
    public static void main(String[] args) {
        Configuration.init();
        ExtractService extractor = new ExtractService();
        extractor.start();
    }
}
