package com.Wind.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接受json所需类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseEntity {
    private int errorCode;
    private int status;
    private String message;
    private Object source;
}

