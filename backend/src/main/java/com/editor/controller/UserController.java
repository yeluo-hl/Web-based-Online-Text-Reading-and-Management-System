package com.editor.controller;

import com.editor.entity.User;
import com.editor.service.UserService;
import com.editor.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(HttpServletRequest request) {

        Integer Id = (Integer) request.getAttribute("Id");

        return ResponseEntity.ok(userService.getProfile(Id));
    }
}