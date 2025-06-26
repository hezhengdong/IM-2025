package org.example.im.message;

import jakarta.servlet.http.HttpServletRequest;
import org.example.im.chat.MessageResponse;
import org.example.im.common.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/{roomId}/messages")
    public Result getMessages(@PathVariable Integer roomId, HttpServletRequest request) {
        Integer userId = Integer.valueOf(request.getHeader("userId"));
        return Result.success(messageService.getMessages(roomId, userId));
    }
}
