package api.socialmedia.service.impl;


import api.socialmedia.dto.responce.PostResponseDto;
import api.socialmedia.entity.Friendship;
import api.socialmedia.entity.Post;
import api.socialmedia.entity.User;
import api.socialmedia.exception.UserNotFoundException;
import api.socialmedia.mapper.PostMapper;
import api.socialmedia.repository.FriendshipRepository;
import api.socialmedia.repository.PostRepository;
import api.socialmedia.repository.UserRepository;
import api.socialmedia.service.PostFeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostFeedServiceImpl implements PostFeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PostMapper postMapper;
    private final String FILE_URL = "http://localhost:8080/file/download/";

    /**
     * Получение ленты постов
     * Реализация:
     * Настраивает пагинацию по умолчанию и находим юзера по id
     * Получает id всех друзей пользователя
     * Получает ленту активности в соответствии с условием - поддерживать пагинацию и сортировку по времени создания
     * Сортировка по времени создания задается также в объекте Pageable
     */

    @Transactional(readOnly = true)
    public List<PostResponseDto> getUserActivityFeed(Long userId, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "date"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Long> friendsIds = getFriendIds(userId);
        Page<Post> activityFeed = postRepository.findByUserIdIn(friendsIds, pageable);
        return activityFeed.stream()
                .map(postMapper::postEntityToPostResponseDto)
                .peek(responseDto -> responseDto.setFileUrl(FILE_URL + responseDto.getId()))
                .collect(Collectors.toList());
    }

    private List<Long> getFriendIds(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByUserOneIdOrUserTwoId(userId, userId);
        return friendships.stream()
                .flatMap(friendship -> Stream.of(friendship.getUserOne().getId(), friendship.getUserTwo().getId()))
                .filter(id -> id != null && !id.equals(userId))
                .collect(Collectors.toList());
    }
}





