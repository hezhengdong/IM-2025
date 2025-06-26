package org.example.im.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MessageResponse {
    private String senderImage;
    private String senderName;
    private String content;
    private String type;
    private String time;
    private String isSelf;
    private Integer roomId;

    // 添加支持LocalDateTime的构造函数
    public MessageResponse(String senderImage, String senderName, String content, String type, LocalDateTime createTime, String isSelf, Integer roomId) {
        this.senderImage = senderImage;
        this.senderName = senderName;
        this.content = content;
        this.type = type;
        this.time = createTime.toString();
        this.isSelf = isSelf;
        this.roomId = roomId;
    }

}
