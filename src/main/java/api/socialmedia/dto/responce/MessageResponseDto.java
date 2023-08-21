package api.socialmedia.dto.responce;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class MessageResponseDto {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private String text;
    private LocalDateTime date;

}
