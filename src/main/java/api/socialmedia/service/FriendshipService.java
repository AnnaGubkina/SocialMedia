package api.socialmedia.service;

import api.socialmedia.dto.request.FriendshipRequestDto;
import api.socialmedia.dto.responce.FollowersResponseDto;
import api.socialmedia.dto.responce.FriendshipResponseDto;

import java.util.List;


public interface FriendshipService {
    FollowersResponseDto sendFriendshipRequest(FriendshipRequestDto request);

    FriendshipResponseDto acceptFriendshipRequest(Long friendshipId);

    void removeFriendship(Long removerId, Long deletedId);

    List<FriendshipResponseDto> getFriendshipsForUser(Long userId);

    void removeFollower(Long followerId);

}
