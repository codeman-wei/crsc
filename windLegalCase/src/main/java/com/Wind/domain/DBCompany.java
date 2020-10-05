package com.Wind.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBCompany {
    private Integer companyId;
    private String companyName;
    private String newestDocDate;
}