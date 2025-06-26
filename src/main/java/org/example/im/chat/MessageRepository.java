package org.example.im.chat;

import org.example.im.common.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 根据房间ID查询消息并关联用户信息
     */
    @Query("""
        SELECT new org.example.im.chat.MessageResponse(
            u.image, 
            u.username, 
            m.content, 
            m.type, 
            m.createTime, 
            CASE WHEN m.senderId = :userId THEN 'true' ELSE 'false' END,
            m.roomId
        ) 
        FROM Message m 
        JOIN User u ON u.id = m.senderId 
        WHERE m.roomId = :roomId 
        ORDER BY m.createTime ASC
    """)
    List<MessageResponse> findMessagesByRoomIdWithUserInfo(Integer roomId, Integer userId);
}
