package com.editor.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String name;
    private String userId;
    private String password;
    private String role;
}