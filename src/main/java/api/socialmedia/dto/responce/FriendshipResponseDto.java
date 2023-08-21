package api.socialmedia.dto.responce;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendshipResponseDto {
    private Long id;
    private Long userOneId;
    private Long userTwoId;
    private LocalDateTime date;
}