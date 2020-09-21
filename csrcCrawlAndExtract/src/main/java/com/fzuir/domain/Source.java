package com.fzuir.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Source {
    private String url;
    private String from;
    private String libraryType;
}
