package com.editor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.editor.mapper")
public class TextEditorApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TextEditorApplication.class, args);
    }
}
