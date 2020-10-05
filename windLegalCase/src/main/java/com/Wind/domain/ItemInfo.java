package com.Wind.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemInfo {
    private String corpName;
    private String caseName;
    private String caseId;
    private String caseReason;
    private String caseAmount;
    private String plaintiff;
    private String defendant;
    private String agent;
    private String thirdParties;
    private String judgeResult;
    private String judgeDetail;
    private String judgeDate;
    private String province;
    private String court;
    private String pubDate;
    private String docType;
}

