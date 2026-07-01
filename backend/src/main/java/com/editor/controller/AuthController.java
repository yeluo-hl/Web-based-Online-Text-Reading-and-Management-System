package com.editor.controller;

import com.editor.dto.LoginRequest;
import com.editor.dto.RegisterRequest;
import com.editor.service.UserService;
import com.editor.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        ApiResponse<Map<String, Object>> response = userService.login(
                request.getUserId(),
                request.getPassword(),
                request.getRole()
        );

        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody RegisterRequest request) {
        ApiResponse<Map<String, Object>> response = userService.register(
                request.getUserId(),
                request.getEmail(),
                request.getName(),
                request.getPassword(),
                request.getRole()
        );

        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Object>> health() {
        Map<String, Object> data = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", "UP"
        );
        return ResponseEntity.ok(ApiResponse.success("Service is running", data));
    }
}