package com.editor.service;

import com.editor.mapper.AdminMapper;
import com.editor.mapper.DocumentMapper;
import com.editor.util.ApiResponse;
import com.editor.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper    adminMapper;
    private final DocumentMapper documentMapper;
    private final PasswordUtil   passwordUtil;

    public ApiResponse<Map<String, Object>> getSiteStats() {
        Map<String, Object> stats = adminMapper.getSiteStats();
        return ApiResponse.success(stats);
    }

    public ApiResponse<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = adminMapper.getAllUsersWithDocCount();
        return ApiResponse.success(users);
    }

    // 删除用户：管理员不可自删，同时清除其所有文档
    @Transactional
    public ApiResponse<String> deleteUser(Integer targetId, Integer operatorId) {
        if (targetId.equals(operatorId)) {
            return ApiResponse.error("不能删除自己");
        }
        // 先删文档（若数据库已设 ON DELETE CASCADE 可省略）
        documentMapper.deleteAllByUId(targetId);
        int rows = adminMapper.deleteUser(targetId);
        return rows > 0 ? ApiResponse.success("用户已删除") : ApiResponse.error("用户不存在");
    }

    // 重置密码：新密码至少 6 位，自动 BCrypt 编码后写入
    public ApiResponse<String> resetPassword(Integer targetId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return ApiResponse.error("密码至少 6 位");
        }
        String encoded = passwordUtil.encode(newPassword);
        int rows = adminMapper.updatePassword(targetId, encoded);
        return rows > 0 ? ApiResponse.success("密码已重置") : ApiResponse.error("用户不存在");
    }
}