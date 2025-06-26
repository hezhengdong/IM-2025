package org.example.im.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room_friend")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomFriend {

    @Id
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_a_id", nullable = false)
    private Integer userAId;

    @Column(name = "user_b_id", nullable = false)
    private Integer userBId;

    @Column(name = "remark_a", length = 255)
    private String remarkA;

    @Column(name = "remark_b", length = 255)
    private String remarkB;
}
