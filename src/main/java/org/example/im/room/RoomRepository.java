package org.example.im.room;

import org.example.im.common.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    /**
     * 根据房间ID查询房间类型
     */
    @Query("SELECT r.type FROM Room r WHERE r.id = :roomId")
    Integer findTypeById(Integer roomId);

    /**
     * 查找活跃房间（按最后活跃时间排序）
     */
    @Query("SELECT new org.example.im.room.RoomTemporaryResponse(r.id, r.type, r.activeTime, r.lastMsgId, rf.userAId, rf.userBId, rf.remarkA, rf.remarkB) " +
            "FROM Room r JOIN RoomFriend rf ON r.id = rf.roomId " +
            "WHERE (rf.userAId = :userId OR rf.userBId = :userId) " +
            "ORDER BY r.activeTime DESC")
    List<RoomTemporaryResponse> findActiveRoomsOrderByActiveTime(Integer userId);

       /**
     * 更新房间的活跃时间和最后消息ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Room r SET r.activeTime = :activeTime, r.lastMsgId = :lastMsgId WHERE r.id = :roomId")
    void updateRoomActiveTimeAndLastMsg(@Param("roomId") Integer roomId, 
                                       @Param("activeTime") LocalDateTime activeTime, 
                                       @Param("lastMsgId") Long lastMsgId);
}
