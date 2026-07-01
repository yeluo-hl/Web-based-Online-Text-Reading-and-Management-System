package com.editor.controller;

import com.editor.service.AdminService;
import com.editor.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 全站统计
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(adminService.getSiteStats());
    }

    // 所有用户列表（含文档数）
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // 删除用户
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Integer id,
            HttpServletRequest request) {
        Integer operatorId = (Integer) request.getAttribute("Id");
        return ResponseEntity.ok(adminService.deleteUser(id, operatorId));
    }

    // 重置用户密码
    @PutMapping("/users/{id}/password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.resetPassword(id, body.get("password")));
    }
}