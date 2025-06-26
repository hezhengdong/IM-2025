package org.example.im.message;

import org.example.im.chat.MessageRepository;
import org.example.im.chat.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public List<MessageResponse> getMessages(Integer roomId, Integer userId) {
        List<MessageResponse> messages = messageRepository.findMessagesByRoomIdWithUserInfo(roomId, userId);
        for (MessageResponse message : messages) {
            System.out.println(message);
        }
        return messages;
    }
}
