package com.editor.mapper;

import com.editor.entity.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM users WHERE userId = #{userId} OR email = #{userId}")
    User findByUserIdOrEmail(@Param("userId") String userId);
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(@Param("id") Integer id);
    
    @Select("SELECT * FROM users WHERE userId = #{userId}")
    User findByUserId(@Param("userId") String userId);
    
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(@Param("email") String email);
    
    @Insert("INSERT INTO users (userId, email, name, password, role) VALUES (#{userId}, #{email}, #{name}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE users SET name = #{name}, email = #{email} WHERE id = #{id}")
    int update(User user);
    
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);
}
