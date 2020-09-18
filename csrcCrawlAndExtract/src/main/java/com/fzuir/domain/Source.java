package com.fzuir.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Source {
    private Integer id;
    private String url;
    private String source;
    private String libraryType;
}
