package com.editor.service;

import com.editor.entity.Document;
import com.editor.mapper.DocumentMapper;
import com.editor.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentMapper documentMapper;

    @Transactional
    public ApiResponse<Document> saveDocument(String id, Integer uId, String title, String content, Integer wordCount) {

        // 允许 TipTap 空文档（如 "<p></p>"）通过，只拒绝 null
        if (content == null) {
            return ApiResponse.error("文档内容不能为空");
        }

        Document doc = new Document();
        doc.setId(id != null ? id : UUID.randomUUID().toString());
        doc.setUId(uId);
        doc.setTitle(title == null || title.trim().isEmpty() ? "未命名文档" : title);
        doc.setContent(content);
        doc.setWordCount(wordCount == null ? 0 : wordCount);

        if (id != null) {
            if (documentMapper.update(doc) == 0) {
                return ApiResponse.error("提交失败");
            }
        } else {
            documentMapper.insert(doc);
        }

        return ApiResponse.success(doc);
    }

    @Transactional
    public ApiResponse<Document> updateTitle(String id, Integer uId, String title) {
        if (title == null || title.trim().isEmpty()) {
            return ApiResponse.error("标题不能为空");
        }
        int rows = documentMapper.updateTitle(id, uId, title.trim());
        if (rows == 0) return ApiResponse.error("文档不存在");
        Document doc = documentMapper.findByIdAnduId(id, uId);
        return ApiResponse.success(doc);
    }

    public ApiResponse<Map<String, Object>> getDocumentList(Integer uId, Integer page, Integer limit, String sortBy, String order) {
        int offset = (page - 1) * limit;
        List<Document> list = documentMapper.findByuId(uId, limit, offset);
        int total = documentMapper.countByuId(uId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        return ApiResponse.success(data);
    }

    public ApiResponse<Document> getDocument(String id, Integer uId) {
        Document doc = documentMapper.findByIdAnduId(id, uId);
        return doc == null ? ApiResponse.error("未找到") : ApiResponse.success(doc);
    }

    @Transactional
    public ApiResponse<String> deleteDocument(String id, Integer uId) {
        return documentMapper.deleteByIdAnduId(id, uId) > 0
                ? ApiResponse.success("删除")
                : ApiResponse.error("删除失败");
    }

    // 修复：搜索结果补充 total 字段，前端可做分页
    public ApiResponse<Map<String, Object>> searchDocuments(Integer uId, String q, Integer page, Integer limit) {
        if (q == null || q.trim().isEmpty()) {
            return getDocumentList(uId, page, limit, null, null);
        }

        int offset = (page - 1) * limit;
        String keyword = q.trim();

        List<Document> list  = documentMapper.searchByuId(uId, keyword, limit, offset);
        int            total = documentMapper.countSearchByuId(uId, keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("list",    list);
        data.put("total",   total);
        data.put("keyword", keyword);   // 返回关键词，前端高亮用

        return ApiResponse.success(data);
    }

    public ApiResponse<Map<String, Object>> getDocumentStats(Integer uId) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalDocuments", documentMapper.countByuId(uId));
        return ApiResponse.success(data);
    }

    @Transactional
    public ApiResponse<Map<String, Object>> batchDeleteDocuments(List<String> ids, Integer uId) {
        int success = 0;
        for (String id : ids) {
            if (documentMapper.deleteByIdAnduId(id, uId) > 0) success++;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("success", success);
        return ApiResponse.success(data);
    }
}