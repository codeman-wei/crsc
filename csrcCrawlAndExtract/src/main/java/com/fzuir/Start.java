package com.fzuir;

import com.fzuir.service.ExtractService;


public class Start {
    public static void main(String[] args) {
        ExtractService extractor = new ExtractService();
        extractor.start();
    }
}
