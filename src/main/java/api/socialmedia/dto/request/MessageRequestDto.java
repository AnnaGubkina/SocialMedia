package api.socialmedia.dto.request;

import lombok.Data;



@Data
public class MessageRequestDto {
    private Long senderId;
    private Long receiverId;
    private String text;
}