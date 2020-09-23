package com.fzuir.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostData {
    // 搜索的词
    private String schword;
    // 来源网站标识
    private String searchword;
    // 来源站点
    private String from;
    // 文书类型
    private String libraryType;
}
