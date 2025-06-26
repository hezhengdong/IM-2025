package org.example.im.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {
    private Long id;
    private Integer type;
    private String name;
    private String activeTime;
    private String lastMsg;
}
