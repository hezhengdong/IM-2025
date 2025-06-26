package org.example.im.chat;

import org.example.im.common.entity.RoomFriend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomFriendRepository extends JpaRepository<RoomFriend, Integer> {
}
