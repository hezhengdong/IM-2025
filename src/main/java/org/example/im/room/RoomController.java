package org.example.im.room;

import jakarta.servlet.http.HttpServletRequest;
import org.example.im.common.dto.Result;
import org.example.im.common.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/rooms")
    public Result getRoomList(HttpServletRequest request) {
        Integer userId = Integer.valueOf(request.getHeader("userId"));
        return Result.success(roomService.getRooms(userId));
    }
}
