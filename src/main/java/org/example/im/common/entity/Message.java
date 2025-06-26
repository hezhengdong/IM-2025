package org.example.im.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", nullable = false)
    private Integer roomId;
    
    @Column(name = "sender_id", nullable = false)
    private Integer senderId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false, length = 20)
    private String type = "text";
    
    @Column(name = "create_time", nullable = false, columnDefinition = "DATETIME(3)")
    private LocalDateTime createTime = LocalDateTime.now();
}
