package api.socialmedia.dto.responce;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class FollowersResponseDto {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private LocalDateTime date;
}
