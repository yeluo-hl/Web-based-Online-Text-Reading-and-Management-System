package com.editor.service;

import com.editor.entity.User;
import com.editor.mapper.UserMapper;
import com.editor.util.JwtUtil;
import com.editor.util.PasswordUtil;
import com.editor.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Transactional
    public ApiResponse<Map<String, Object>> register(String userId, String email, String name, String password, String role) {
        if (userId == null || userId.trim().isEmpty()) {
            return ApiResponse.error("User account cannot be empty");
        }

        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error("Email cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ApiResponse.error("Invalid email format");
        }

        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("Name cannot be empty");
        }

        if (password == null || password.length() < 6) {
            return ApiResponse.error("Password must be at least 6 characters");
        }

        if (userMapper.findByUserId(userId) != null) {
            return ApiResponse.error("User account already exists");
        }

        if (userMapper.findByEmail(email) != null) {
            return ApiResponse.error("Email already exists");
        }

        try {
            User user = new User();
            user.setUserId(userId);
            user.setEmail(email);
            user.setName(name);
            user.setPassword(passwordUtil.encode(password));
            user.setRole(role != null ? role : "user");

            int result = userMapper.insert(user);

            if (result > 0) {
                String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole(), user.getId());

                Map<String, Object> data = new HashMap<>();
                data.put("user", user);
                data.put("token", token);

                return ApiResponse.success("Registration successful", data);
            } else {
                return ApiResponse.error("Registration failed");
            }
        } catch (Exception e) {
            return ApiResponse.error("Registration failed: " + e.getMessage());
        }
    }

    public ApiResponse<Map<String, Object>> login(String userId, String password, String role) {

        if (userId == null || userId.trim().isEmpty()) {
            return ApiResponse.error("User account or email cannot be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            return ApiResponse.error("Password cannot be empty");
        }

        try {
            User user = userMapper.findByUserIdOrEmail(userId);

            if (user == null) {
                return ApiResponse.error("用户不存在");
            }

            if (!passwordUtil.matches(password, user.getPassword())) {
                return ApiResponse.error("密码错误");
            }

            if (role != null && !role.isEmpty() && !role.equals(user.getRole())) {
                return ApiResponse.error("身份错误");
            }

            String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole(), user.getId());

            user.setPassword(null);

            Map<String, Object> data = new HashMap<>();
            data.put("user", user);
            data.put("token", token);

            return ApiResponse.success("登陆成功", data);
        } catch (Exception e) {
            return ApiResponse.error("登陆失败: " + e.getMessage());
        }
    }

    public ApiResponse<User> getProfile(Integer Id) {
        try {
            User user = userMapper.findById(Id);

            if (user == null) {
                return ApiResponse.error("用户不存在");
            }

            user.setPassword(null);

            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<String> updateProfile(Integer Id, String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("名称不能为空");
        }

        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error("邮箱不能为空");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ApiResponse.error("邮箱格式错误");
        }

        try {
            User user = userMapper.findById(Id);

            if (user == null) {
                return ApiResponse.error("用户不存在");
            }

            if (!email.equals(user.getEmail())) {
                User existingUser = userMapper.findByEmail(email);
                if (existingUser != null) {
                    return ApiResponse.error("邮箱已被注册");
                }
            }

            user.setName(name);
            user.setEmail(email);

            int result = userMapper.update(user);

            if (result > 0) {
                return ApiResponse.success("更新成功");   // Bug 4 fix: 原为"注册成功"，语义错误
            } else {
                return ApiResponse.error("更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    public ApiResponse<Map<String, Object>> refreshToken(String token) {
        try {
            String userId = jwtUtil.getUserIdFromToken(token);
            String email  = jwtUtil.getEmailFromToken(token);
            String role   = jwtUtil.getRoleFromToken(token);
            Integer Id    = jwtUtil.getIdFromToken(token);

            String newToken = jwtUtil.generateToken(userId, email, role, Id);
            Map<String, Object> data = new HashMap<>();
            data.put("token", newToken);

            return ApiResponse.success("Token refreshed successfully", data);
        } catch (Exception e) {
            return ApiResponse.error("Failed to refresh token: " + e.getMessage());
        }
    }
}