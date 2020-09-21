package com.fzuir.utils;

import com.fzuir.Start;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class Configuration {
    public static Properties properties;

    public static void init() {
        log.info("begin to load configuration");
        InputStream in = null;
        try {
            properties = new Properties();
            ClassLoader classLoader = Start.class.getClassLoader();
            in = classLoader.getResourceAsStream("system.properties");
            properties.load(in);
            log.info("--------loaded properties");
        } catch (Exception e) {
            log.error("load system configuration fail");
            e.printStackTrace();
            System.exit(0);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }


    public static String getProperty(String key) {
        String s = null;
        if(properties != null){
            s = properties.getProperty(key);
        }
        return s;
    }
}
