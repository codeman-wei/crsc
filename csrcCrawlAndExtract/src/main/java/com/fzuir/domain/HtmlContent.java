package com.fzuir.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HtmlContent {
    private String url;
    private String title;
    private String fileNum;
    private String content;
    private String date;
    private String from;
    private String libtype;
}
