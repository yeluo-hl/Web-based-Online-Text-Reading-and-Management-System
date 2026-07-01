package com.editor.mapper;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface AdminMapper {

    // 全站统计：总用户数、总文档数、总字数、近 7 天新增用户数
    @Select("""
        SELECT
            (SELECT COUNT(*) FROM users)                                               AS totalUsers,
            (SELECT COUNT(*) FROM documents)                                           AS totalDocuments,
            (SELECT COALESCE(SUM(word_count), 0) FROM documents)                       AS totalWords,
            (SELECT COUNT(*) FROM users
             WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY))                     AS newUsersThisWeek,
            (SELECT COUNT(*) FROM documents
             WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY))                     AS newDocsThisWeek
        """)
    Map<String, Object> getSiteStats();

    // 所有用户列表，附带各自文档数量，按注册时间倒序
    @Select("""
        SELECT
            u.id,
            u.userId,
            u.name,
            u.email,
            u.role,
            u.created_at  AS createdAt,
            u.updated_at  AS updatedAt,
            COUNT(d.id)   AS docCount
        FROM users u
        LEFT JOIN documents d ON d.user_id = u.id
        GROUP BY u.id, u.userId, u.name, u.email, u.role, u.created_at, u.updated_at
        ORDER BY u.id ASC
        """)
    List<Map<String, Object>> getAllUsersWithDocCount();

    // 删除用户（同时会因 ON DELETE CASCADE 自动删除其文档，若未设级联则需在 Service 层先删文档）
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteUser(@Param("id") Integer id);

    // 重置密码（传入已 BCrypt 编码后的密文）
    @Update("UPDATE users SET password = #{password}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);
}