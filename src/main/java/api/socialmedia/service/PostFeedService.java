package api.socialmedia.service;

import api.socialmedia.dto.responce.PostResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostFeedService {
    List<PostResponseDto> getUserActivityFeed(Long userId, Pageable pageable);
}
