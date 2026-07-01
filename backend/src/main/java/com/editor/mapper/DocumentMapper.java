package com.editor.mapper;

import com.editor.entity.Document;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface DocumentMapper {


    String BASE_RESULT = "id, user_id AS uId, title, content, word_count AS wordCount, " +
            "created_at AS createdAt, updated_at AS updatedAt";

    @Select("SELECT " + BASE_RESULT + " FROM documents WHERE id = #{id} AND user_id = #{uId}")
    Document findByIdAnduId(@Param("id") String id, @Param("uId") Integer uId);

    @Select("SELECT " + BASE_RESULT + " FROM documents WHERE user_id = #{uId} " +
            "ORDER BY updated_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Document> findByuId(@Param("uId") Integer uId,
                             @Param("limit") Integer limit,
                             @Param("offset") Integer offset);

    @Select("SELECT COUNT(*) FROM documents WHERE user_id = #{uId}")
    int countByuId(@Param("uId") Integer uId);

    @Insert("INSERT INTO documents (id, user_id, title, content, word_count) " +
            "VALUES (#{id}, #{uId}, #{title}, #{content}, #{wordCount})")
    int insert(Document document);

    @Update("UPDATE documents SET title = #{title}, content = #{content}, " +
            "word_count = #{wordCount}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} AND user_id = #{uId}")
    int update(Document document);

    @Update("UPDATE documents SET title = #{title}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} AND user_id = #{uId}")
    int updateTitle(@Param("id") String id,
                    @Param("uId") Integer uId,
                    @Param("title") String title);

    @Delete("DELETE FROM documents WHERE id = #{id} AND user_id = #{uId}")
    int deleteByIdAnduId(@Param("id") String id, @Param("uId") Integer uId);


    @Select("SELECT " + BASE_RESULT + " FROM documents " +
            "WHERE user_id = #{uId} " +
            "AND (" +
            "  title LIKE CONCAT('%', #{query}, '%') " +
            "  OR REGEXP_REPLACE(content, '<[^>]+>', '') LIKE CONCAT('%', #{query}, '%')" +
            ") " +
            "ORDER BY updated_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Document> searchByuId(@Param("uId") Integer uId,
                               @Param("query") String query,
                               @Param("limit") Integer limit,
                               @Param("offset") Integer offset);

    @Select("SELECT COUNT(*) FROM documents " +
            "WHERE user_id = #{uId} " +
            "AND (" +
            "  title LIKE CONCAT('%', #{query}, '%') " +
            "  OR REGEXP_REPLACE(content, '<[^>]+>', '') LIKE CONCAT('%', #{query}, '%')" +
            ")")
    int countSearchByuId(@Param("uId") Integer uId, @Param("query") String query);

    @Select("SELECT * FROM documents WHERE user_id = #{uId} AND " +
            "(title LIKE CONCAT('%', #{query}, '%') OR content LIKE CONCAT('%', #{query}, '%')) " +
            "ORDER BY updated_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Document> searchByuId57(@Param("uId") Integer uId,
                                 @Param("query") String query,
                                 @Param("limit") Integer limit,
                                 @Param("offset") Integer offset);

    @Select("SELECT COUNT(*) as totalDocuments, SUM(word_count) as totalWords, " +
            "COUNT(CASE WHEN created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 END) as recentDocuments " +
            "FROM documents WHERE user_id = #{uId}")
    Map<String, Object> getStatsByuId(@Param("uId") Integer uId);

    // 删除某用户的全部文档（管理员删除用户时调用）
    @Delete("DELETE FROM documents WHERE user_id = #{uId}")
    int deleteAllByUId(@Param("uId") Integer uId);
}