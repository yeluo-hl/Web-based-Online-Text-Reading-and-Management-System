package com.editor.dto;

import lombok.Data;

@Data
public class DocumentUpdateRequest {
    private String title;
    private String content;
    private Integer wordCount;
}