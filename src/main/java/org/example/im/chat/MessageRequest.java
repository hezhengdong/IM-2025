package org.example.im.chat;

import lombok.Data;

@Data
public class MessageRequest {
    private String type; // text file
    private Integer roomId; // 房间 ID
    private String content; // 消息内容
}
