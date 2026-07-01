package com.editor.dto;

import lombok.Data;

// 协同编辑 WebSocket 消息体
@Data
public class CollaborationMessage {

    // 消息类型: join | leave | change | ping
    private String type;

    // 文档 ID
    private String docId;

    // 发送方用户账号
    private String userId;

    // 发送方用户昵称
    private String userName;

    // 用户标识色（十六进制，如 #3b82f6）
    private String color;

    // type=change 时携带的 HTML 内容
    private String content;

    // 文档标题（type=change 时同步标题）
    private String title;

    // 服务端时间戳（毫秒）
    private Long timestamp;

    // 是否含图片（true = 接收方需从数据库拉取完整内容）
    private Boolean hasImage;

    // 含图片时携带的文档 ID，接收方用于拉取完整内容
    private String imageDocId;
}