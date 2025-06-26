package org.example.im.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.example.im.auth.AuthService;
import org.example.im.auth.UserRepository;
import org.example.im.common.entity.Message;
import org.example.im.common.entity.RoomFriend;
import org.example.im.common.entity.User;
import org.example.im.room.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 存储所有连接的用户
    private final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 用户ID与Channel的映射
    private final ConcurrentHashMap<Integer, Channel> userIdToChannelMap = new ConcurrentHashMap<>();

    @Autowired
    private RoomFriendRepository roomFriendRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    /**
     * 连接
     */
    public void connect(Integer userId, Channel channel) {
        userIdToChannelMap.put(userId, channel);
        allChannels.add(channel);
        logger.info("建立用户ID与Channel的映射: userId={}, channelId={}", userId, channel.id().asShortText());
    }

    /**
     * 断开连接
     */
    public void disconnect(Integer userId, Channel channel) {
        userIdToChannelMap.remove(userId);
        allChannels.remove(channel);
        logger.info("删除用户ID与Channel的映射: userId={}, channelId={}", userId, channel.id().asShortText());
    }

    /**
     * 发送私聊消息
     */
    public boolean sendPrivateMessage(Integer senderId, Integer roomId, String content, String messageType) {
        try {
            logger.info("房间号: {} 单聊消息发送中...", roomId);
            // 从数据库中获取接收人 ID
            Optional<RoomFriend> roomFriendOpt = roomFriendRepository.findById(roomId);
            if (roomFriendOpt.isEmpty()) {
                return false;
            }
            RoomFriend roomFriend = roomFriendOpt.get();
            Integer userAId = roomFriend.getUserAId();
            Integer userBId = roomFriend.getUserBId();
            Integer receiverId = userAId.equals(senderId) ? userBId : userAId;

            // 构建发送消息
            Optional<User> senderOpt = userRepository.findById(senderId);
            if (senderOpt.isEmpty()) {
                return false;
            }
            User sender = senderOpt.get();
            MessageResponse response = new MessageResponse(sender.getImage(), sender.getUsername(), content, messageType, LocalDateTime.now(), "false", roomId);

            // 如果目标用户在线，通过 channel 发送消息
            Channel targetChannel = getUserChannel(receiverId);
            //if (targetChannel != null && targetChannel.isActive()) {
            if (targetChannel != null) {
                String jsonMessage = objectMapper.writeValueAsString(response);
                targetChannel.writeAndFlush(new TextWebSocketFrame(jsonMessage));
                logger.info("已实时发送单聊消息: {} -> {}", senderId, receiverId);
            }

            // 将消息保存至数据库
            Message savedMessage = messageRepository.save(new Message(null, roomId, senderId, content, messageType, LocalDateTime.now()));
            
            // 更新房间的活跃时间和最后消息ID
            roomRepository.updateRoomActiveTimeAndLastMsg(roomId, LocalDateTime.now(), savedMessage.getId());
            
            logger.info("单聊消息发送成功，已保存至数据库: 房间号 {} 发送人ID {}", roomId, senderId);
            return true;
        } catch (Exception e) {
            logger.error("发送单聊消息失败: {}", e.getMessage());
            return false;
        }
    }

    public boolean sendGroupMessage(Integer senderId, Integer roomId, String content, String messageType) {
        // TODO: 群聊功能
        return true;
    }

    /**
     * 广播消息（相当与一个包含所有人的群组）
     */
    public boolean broadcastMessage(Integer senderId, Integer roomId, String content, String messageType) {
        try {
            logger.info("房间号: {} 广播消息发送中...", roomId);

            // 构建发送消息
            Optional<User> senderOpt = userRepository.findById(senderId);
            if (senderOpt.isEmpty()) {
                logger.error("发送者不存在: senderId={}", senderId);
                return false;
            }
            User sender = senderOpt.get();
            MessageResponse response = new MessageResponse(sender.getImage(), sender.getUsername(), content, messageType, LocalDateTime.now(), "false", roomId);

            // 将消息转换为JSON
            String jsonMessage = objectMapper.writeValueAsString(response);

            // 向所有在线用户广播消息（除了发送者自己）
            int sentCount = 0;
            Channel senderChannel = getUserChannel(senderId);

            for (Channel channel : allChannels) {
                if (channel != null && channel.isActive() && !channel.equals(senderChannel)) {
                    channel.writeAndFlush(new TextWebSocketFrame(jsonMessage));
                    sentCount++;
                }
            }

            logger.info("广播消息已发送给 {} 个在线用户（排除发送者）", sentCount);

            // 将消息保存至数据库
            Message savedMessage = messageRepository.save(new Message(null, roomId, senderId, content, messageType, LocalDateTime.now()));
            
            // 更新房间的活跃时间和最后消息ID
            roomRepository.updateRoomActiveTimeAndLastMsg(roomId, LocalDateTime.now(), savedMessage.getId());
            
            logger.info("广播消息发送成功，已保存至数据库: 房间号 {} 发送人ID {}", roomId, senderId);

            return true;
        } catch (Exception e) {
            logger.error("发送广播消息失败: {}", e.getMessage(), e);
            return false;
        }
    }

    // 管理 channel 连接
    public Channel getUserChannel(Integer userId) {
        return userIdToChannelMap.get(userId);
    }
}
