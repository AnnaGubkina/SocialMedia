package api.socialmedia.dto.request;

import lombok.Data;

@Data
public class FriendshipRequestDto {
    private Long senderId;
    private Long receiverId;
}
