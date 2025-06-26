package org.example.im.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomTemporaryResponse {
    private Long id;
    private Integer type;
    private LocalDateTime activeTime;
    private Long lastMsgId;
    private Integer userAId;
    private Integer userBId;
    private String remarkA;
    private String remarkB;
}
