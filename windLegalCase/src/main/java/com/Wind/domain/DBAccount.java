package com.Wind.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBAccount {
    private String account;
    private String password;
    private String postUrl;
    private String docType;

}
