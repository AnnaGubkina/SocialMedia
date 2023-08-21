package api.socialmedia.service;

import api.socialmedia.dto.responce.PostResponseDto;
import api.socialmedia.entity.Friendship;
import api.socialmedia.entity.Post;
import api.socialmedia.entity.User;
import api.socialmedia.exception.UserNotFoundException;
import api.socialmedia.mapper.PostMapper;
import api.socialmedia.repository.FriendshipRepository;
import api.socialmedia.repository.PostRepository;
import api.socialmedia.repository.UserRepository;
import api.socialmedia.service.impl.PostFeedServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing Feed service functionality.")
public class PostFeedServiceImplTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private PostMapper postMapper;
    @InjectMocks
    private PostFeedServiceImpl postFeedService;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("Получение ленты активности пользователя. Должно пройти успешно.")
    public void testGetUserActivityFeed() {
        // Создаем тестовые данные
        User user = new User();
        user.setId(USER_ID);

        User user2 = new User();
        user2.setId(2L);

        User user3 = new User();
        user3.setId(3L);

        Friendship friendship1 = new Friendship();
        friendship1.setUserOne(user);
        friendship1.setUserTwo(user2);

        Friendship friendship2 = new Friendship();
        friendship2.setUserOne(user);
        friendship2.setUserTwo(user3);

        List<Friendship> friendships = new ArrayList<>();
        friendships.add(friendship1);
        friendships.add(friendship2);

        Post post1 = new Post();
        post1.setId(1L);
        post1.setDate(LocalDateTime.now().minusHours(1));

        Post post2 = new Post();
        post2.setId(2L);
        post2.setDate(LocalDateTime.now());

        List<Post> posts = Arrays.asList(post1, post2);

        // Создаем список тестовых id пользователей
        List<Long> friendIds = Arrays.asList(2L, 3L);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));

        Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());


        // мокируем репозитории и маппер
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(friendshipRepository.findByUserOneIdOrUserTwoId(USER_ID, USER_ID)).thenReturn(friendships);
        when(postRepository.findByUserIdIn(friendIds,pageable)).thenReturn(postPage);
        when(postMapper.postEntityToPostResponseDto(any(Post.class))).thenReturn(new PostResponseDto());

        List<PostResponseDto> result = postFeedService.getUserActivityFeed(USER_ID, pageable);

        verify(userRepository).findById(USER_ID);
        verify(friendshipRepository).findByUserOneIdOrUserTwoId(USER_ID, USER_ID);
        verify(postRepository).findByUserIdIn(eq(friendIds), eq(pageable));
        verify(postMapper, times(posts.size())).postEntityToPostResponseDto(any(Post.class));

        assertNotNull(result);
        assertEquals(posts.size(), result.size());
    }

    @Test
    @DisplayName("Получение ленты активности пользователя - пользователь не найден")
    public void testGetUserActivityFeed_UserNotFound() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> postFeedService.getUserActivityFeed(userId, pageable));

        verify(userRepository).findById(userId);
    }
}
