-- 创建数据库
CREATE DATABASE IF NOT EXISTS homework_im;

-- 切换到该数据库
USE homework_im;

-- 用户表 (user)
CREATE TABLE user (
                      id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
                      username VARCHAR(50) NOT NULL COMMENT '用户名',
                      password VARCHAR(255) NOT NULL COMMENT '密码',
                      image VARCHAR(255) DEFAULT NULL COMMENT '头像URL地址',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      UNIQUE KEY uniq_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 房间表 (room)
CREATE TABLE room (
                      id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '房间ID',
                      type TINYINT NOT NULL DEFAULT 0 COMMENT '类型：0-单聊 1-群聊',
                      active_time DATETIME DEFAULT NULL COMMENT '最后活跃时间（群最后消息时间）',
                      last_msg_id INT UNSIGNED DEFAULT 0 COMMENT '最后消息ID',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      KEY idx_active_time (active_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天房间表';

-- 房间表初始数据
INSERT INTO room (id, type, active_time, last_msg_id, create_time)
VALUES (1, 1, NOW(), 0, NOW());

-- 消息表 (message)
CREATE TABLE message (
                         id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
                         room_id INT UNSIGNED NOT NULL COMMENT '所属房间ID',
                         sender_id INT UNSIGNED NOT NULL COMMENT '发送者用户ID',
                         content TEXT NOT NULL COMMENT '消息内容',
                         type VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '消息类型（text、image、file等）',
                         create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '精确到毫秒的创建时间',
                         KEY idx_room (room_id),
                         KEY idx_sender (sender_id),
                         KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 单聊房间关联表 (room_friend)
CREATE TABLE IF NOT EXISTS `room_friend` (
                                             room_id INT UNSIGNED NOT NULL COMMENT '房间ID',
                                             user_a_id INT UNSIGNED NOT NULL COMMENT '用户A ID',
                                             user_b_id INT UNSIGNED NOT NULL COMMENT '用户B ID',
                                             remark_a VARCHAR(255) DEFAULT NULL COMMENT '用户A设置的备注信息',
    remark_b VARCHAR(255) DEFAULT NULL COMMENT '用户B设置的备注信息',
    PRIMARY KEY (room_id),
    UNIQUE KEY uk_friend_pair (user_a_id, user_b_id),
    KEY idx_user_a (user_a_id),
    KEY idx_user_b (user_b_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单聊房间成员关系表';
