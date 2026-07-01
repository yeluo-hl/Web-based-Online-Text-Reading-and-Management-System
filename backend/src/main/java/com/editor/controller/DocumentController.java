package com.editor.controller;

import com.editor.dto.DocumentCreateRequest;
import com.editor.dto.DocumentUpdateRequest;
import com.editor.entity.Document;
import com.editor.service.DocumentService;
import com.editor.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // 创建文档
    @PostMapping
    public ResponseEntity<ApiResponse<Document>> create(@RequestBody DocumentCreateRequest request,
                                                        HttpServletRequest httpRequest) {
        Integer uId = (Integer) httpRequest.getAttribute("uId");
        return ResponseEntity.ok(
                documentService.saveDocument(null, uId, request.getTitle(), request.getContent(), request.getWordCount())
        );
    }

    // 更新文档（内容 + 标题）
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Document>> update(@PathVariable String id,
                                                        @RequestBody DocumentUpdateRequest request,
                                                        HttpServletRequest httpRequest) {
        Integer uId = (Integer) httpRequest.getAttribute("uId");
        return ResponseEntity.ok(
                documentService.saveDocument(id, uId, request.getTitle(), request.getContent(), request.getWordCount())
        );
    }

    // 仅更新标题
    @PatchMapping("/{id}/title")
    public ResponseEntity<ApiResponse<Document>> updateTitle(@PathVariable String id,
                                                             @RequestBody Map<String, String> body,
                                                             HttpServletRequest httpRequest) {
        Integer uId = (Integer) httpRequest.getAttribute("uId");
        return ResponseEntity.ok(
                documentService.updateTitle(id, uId, body.get("title"))
        );
    }

    // 获取文档列表（分页）
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        Integer uId = (Integer) request.getAttribute("uId");
        return ResponseEntity.ok(
                documentService.getDocumentList(uId, page, limit, null, null)
        );
    }

    // 新增：关键词搜索（标题 + 内容）
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        Integer uId = (Integer) request.getAttribute("uId");
        return ResponseEntity.ok(
                documentService.searchDocuments(uId, q, page, limit)
        );
    }

    // 获取单个文档
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Document>> get(@PathVariable String id,
                                                     HttpServletRequest request) {
        Integer uId = (Integer) request.getAttribute("uId");
        return ResponseEntity.ok(
                documentService.getDocument(id, uId)
        );
    }

    // 删除文档
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable String id,
                                                      HttpServletRequest request) {
        Integer uId = (Integer) request.getAttribute("uId");
        return ResponseEntity.ok(
                documentService.deleteDocument(id, uId)
        );
    }

    // 批量删除
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchDelete(@RequestBody List<String> ids,
                                                                        HttpServletRequest request) {
        Integer uId = (Integer) request.getAttribute("uId");
        return ResponseEntity.ok(
                documentService.batchDeleteDocuments(ids, uId)
        );
    }
}