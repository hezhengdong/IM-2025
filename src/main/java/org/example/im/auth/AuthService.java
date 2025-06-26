package org.example.im.auth;

import cn.hutool.core.util.IdUtil;
import org.example.im.chat.RoomFriendRepository;
import org.example.im.common.entity.Room;
import org.example.im.common.entity.RoomFriend;
import org.example.im.common.entity.User;
import org.example.im.room.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired private UserRepository userRepository;

    @Autowired private RoomRepository roomRepository;
    @Autowired
    private RoomFriendRepository roomFriendRepository;

    public boolean register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            logger.warn("用户名已存在: {}", username);
            return false;
        }
        userRepository.save(new User(null, username, password, "default.jpg", LocalDateTime.now()));
        logger.info("用户注册成功: {}", username);

        // 用户注册成功后，为该用户与其他所有用户创建单聊房间
        Integer selfUserId = userRepository.findByUsername(username).getId(); // 获取自己的ID
        List<User> users = userRepository.findAllExcept(selfUserId); // 获取除自己外的所有用户信息
        for (User user : users) {
            Room room = roomRepository.save(new Room(null, 0, LocalDateTime.now(), 0L, LocalDateTime.now()));
            roomFriendRepository.save(new RoomFriend(room.getId(), user.getId(), selfUserId, user.getUsername(), username));
        }

        return true;
    }

    public Integer login(String username, String password) {
        // 如果用户名不存在，返回 false
        if (!userRepository.existsByUsername(username)) {
            logger.warn("用户不存在: {}", username);
            return null;
        }
        // 如果用户名存在，但密码不正确，返回 false
        User user = userRepository.findByUsername(username);
        if (!user.getPassword().equals(password)) {
            logger.warn("用户密码错误: {}", username);
            return null;
        }
        logger.info("用户登录成功: {}", username);
        return user.getId();
    }
}
