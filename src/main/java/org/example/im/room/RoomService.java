package org.example.im.room;

import org.example.im.chat.MessageRepository;
import org.example.im.common.entity.Message;
import org.example.im.common.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class RoomService {

    @Autowired private RoomRepository roomRepository;

    @Autowired private MessageRepository messageRepository;

    public List<RoomResponse> getRooms(Integer userId) {
        List<RoomTemporaryResponse> roomTemps = roomRepository.findActiveRoomsOrderByActiveTime(userId);
        List<RoomResponse> roomResponses = new ArrayList<>();
        // 返回单聊房间信息
        for (RoomTemporaryResponse roomTemp : roomTemps) {
            String lastMsg = "";
            if (roomTemp.getLastMsgId() != 0) {
                lastMsg = messageRepository.getById(roomTemp.getLastMsgId()).getContent();
            }
            String name = Objects.equals(userId, roomTemp.getUserAId()) ? roomTemp.getRemarkB() : roomTemp.getRemarkA();
            RoomResponse roomResponse = new RoomResponse(roomTemp.getId(), roomTemp.getType(), name, roomTemp.getActiveTime().toString(), lastMsg);
            roomResponses.add(roomResponse);
        }
        // 返回公共房间信息
        Room commonRoom = roomRepository.getById(1);
        String lastMsg = "";
        if (commonRoom.getLastMsgId() != 0) {
            lastMsg = messageRepository.getById(commonRoom.getLastMsgId()).getContent();
        }
        RoomResponse commonRoomResponse = new RoomResponse(1L, 1, "公共群组", commonRoom.getActiveTime().toString(), lastMsg);
        roomResponses.add(commonRoomResponse);
        return roomResponses;
    }
}
