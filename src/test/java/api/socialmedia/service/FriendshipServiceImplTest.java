package api.socialmedia.service;


import api.socialmedia.dto.request.FriendshipRequestDto;
import api.socialmedia.dto.responce.FollowersResponseDto;
import api.socialmedia.dto.responce.FriendshipResponseDto;
import api.socialmedia.entity.Follower;
import api.socialmedia.entity.Friendship;
import api.socialmedia.entity.User;
import api.socialmedia.exception.DuplicateFriendshipRequestException;
import api.socialmedia.exception.FriendshipNotFoundException;
import api.socialmedia.exception.RequestNotFoundException;
import api.socialmedia.repository.FollowerRepository;
import api.socialmedia.repository.FriendshipRepository;
import api.socialmedia.repository.UserRepository;
import api.socialmedia.service.impl.FriendshipServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing Friendship service functionality.")
public class FriendshipServiceImplTest {

    @InjectMocks
    private FriendshipServiceImpl friendshipService;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private FollowerRepository followerRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Отправка запроса на добавление в друзья. Должно пройти успешно")
    public void testSendFriendshipRequest() {
        // создаем тестовые данные
        FriendshipRequestDto requestDto = new FriendshipRequestDto();
        requestDto.setSenderId(1L);
        requestDto.setReceiverId(2L);

        User sender = new User();
        sender.setId(1L);

        User receiver = new User();
        receiver.setId(2L);

        Follower follower = new Follower();
        follower.setSender(sender);
        follower.setReceiver(receiver);


        when(followerRepository.existsBySenderIdAndReceiverId(sender.getId(), receiver.getId())).thenReturn(false);
        when(followerRepository.save(any(Follower.class))).thenReturn(follower);
        when(userRepository.getById(1L)).thenReturn(sender);
        when(userRepository.getById(2L)).thenReturn(receiver);

        FollowersResponseDto responseDto = friendshipService.sendFriendshipRequest(requestDto);

        verify(userRepository).getById(1L);
        verify(followerRepository).existsBySenderIdAndReceiverId(1L, 2L);
        verify(followerRepository).save(any(Follower.class));

        assertNotNull(responseDto);
    }

    @Test
    @DisplayName("Дублирование запроса на дружбу. Должно вернуть ошибку")
    public void testSendDuplicateFriendshipRequest() {
        FriendshipRequestDto requestDto = new FriendshipRequestDto();
        requestDto.setSenderId(1L);
        requestDto.setReceiverId(2L);

        when(followerRepository.existsBySenderIdAndReceiverId(1L, 2L)).thenReturn(true);

        assertThrows(DuplicateFriendshipRequestException.class, () -> friendshipService.sendFriendshipRequest(requestDto));
        verify(followerRepository).existsBySenderIdAndReceiverId(1L, 2L);

    }

    @Test
    @DisplayName("Подтверждение запроса на дружбу. Должно пройти успешно")
    public void testAcceptFriendshipRequest() {
        Long requestId = 1L;

        User sender = new User();
        sender.setId(1L);

        User receiver = new User();
        receiver.setId(2L);

        Follower follower = new Follower();
        follower.setId(requestId);
        follower.setSender(sender);
        follower.setReceiver(receiver);

        Friendship newFriendship = new Friendship();
        newFriendship.setId(1L);
        newFriendship.setUserOne(sender);
        newFriendship.setUserTwo(receiver);

        when(followerRepository.findById(requestId)).thenReturn(Optional.of(follower));
        when(friendshipRepository.existsByUserOneIdAndUserTwoId(sender.getId(), receiver.getId())).thenReturn(false);
        when(friendshipRepository.existsByUserOneIdAndUserTwoId(receiver.getId(), sender.getId())).thenReturn(false);
        when(followerRepository.existsByReceiverIdAndSenderId(sender.getId(), receiver.getId())).thenReturn(false);
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(newFriendship);
        when(followerRepository.save(any(Follower.class))).thenReturn(follower);

        FriendshipResponseDto responseDto = friendshipService.acceptFriendshipRequest(requestId);

        verify(followerRepository).findById(requestId);
        verify(friendshipRepository).existsByUserOneIdAndUserTwoId(sender.getId(), receiver.getId());
        verify(friendshipRepository).existsByUserOneIdAndUserTwoId(receiver.getId(), sender.getId());
        verify(followerRepository).existsByReceiverIdAndSenderId(sender.getId(), receiver.getId());
        verify(friendshipRepository).save(any(Friendship.class));
        verify(followerRepository).save(any(Follower.class));

        assertNotNull(responseDto);
        assertEquals(newFriendship.getId(), responseDto.getId());
        assertEquals(sender.getId(), responseDto.getUserOneId());
        assertEquals(receiver.getId(), responseDto.getUserTwoId());
    }

    @Test
    @DisplayName("Попытка подтвердить несуществующий запрос на дружбу. Должно вернуть ошибку")
    public void testAcceptNonExistingFriendshipRequest() {
        Long requestId = 1L;

        when(followerRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(RequestNotFoundException.class, () -> friendshipService.acceptFriendshipRequest(requestId));

        verify(followerRepository).findById(requestId);
        verifyNoMoreInteractions(followerRepository, friendshipRepository);
    }

    @Test
    @DisplayName("Дублирование дружбы. Должно вернуть ошибку")
    public void testAcceptDuplicateFriendship() {
        Long requestId = 1L;

        User sender = new User();
        sender.setId(1L);

        User receiver = new User();
        receiver.setId(2L);

        Follower follower = new Follower();
        follower.setId(requestId);
        follower.setSender(sender);
        follower.setReceiver(receiver);

        when(followerRepository.findById(requestId)).thenReturn(Optional.of(follower));
        when(friendshipRepository.existsByUserOneIdAndUserTwoId(sender.getId(), receiver.getId())).thenReturn(true);

        assertThrows(DuplicateFriendshipRequestException.class, () -> friendshipService.acceptFriendshipRequest(requestId));

        verify(followerRepository).findById(requestId);
        verify(friendshipRepository).existsByUserOneIdAndUserTwoId(sender.getId(), receiver.getId());
        verifyNoMoreInteractions(followerRepository, friendshipRepository);
    }

    @Test
    @DisplayName("Удаление дружбы. Должно проходить успешно")
    public void testRemoveFriendship() {
        // Создаем тестовые данные
        Long removerId = 1L;
        Long deletedId = 2L;

        User remover = new User();
        remover.setId(removerId);

        User deleted = new User();
        deleted.setId(deletedId);

        Friendship friendship = new Friendship();
        friendship.setUserOne(remover);
        friendship.setUserTwo(deleted);

        when(friendshipRepository.findByUserOneIdAndUserTwoId(removerId, deletedId)).thenReturn(Optional.of(friendship));
        when(userRepository.getById(removerId)).thenReturn(remover);
        when(userRepository.getById(deletedId)).thenReturn(deleted);

        friendshipService.removeFriendship(removerId, deletedId);

        verify(friendshipRepository).findByUserOneIdAndUserTwoId(removerId, deletedId);
        verify(friendshipRepository).delete(friendship);
        verify(userRepository).getById(removerId);
        verify(userRepository).getById(deletedId);
        verify(followerRepository).deleteBySenderAndReceiver(remover, deleted);
    }

    @Test
    @DisplayName("Попытка удаления несуществующей дружбы. Должна вызвать исключение")
    public void testRemoveNonExistentFriendship() {
        Long removerId = 1L;
        Long deletedId = 2L;

        when(friendshipRepository.findByUserOneIdAndUserTwoId(removerId, deletedId)).thenReturn(Optional.empty());
        when(friendshipRepository.findByUserTwoIdAndUserOneId(removerId, deletedId)).thenReturn(Optional.empty());

        assertThrows(FriendshipNotFoundException.class, () -> {
            friendshipService.removeFriendship(removerId, deletedId);
        });

        verify(friendshipRepository).findByUserOneIdAndUserTwoId(removerId, deletedId);
        verify(friendshipRepository).findByUserTwoIdAndUserOneId(removerId, deletedId);
    }

    @Test
    @DisplayName("Удаление подписки на другого пользователя. Должно пройти успешно")
    public void testRemoveFollowerSuccess() {
        Long followerId = 1L;

        Follower follower = new Follower();
        follower.setId(followerId);

        when(followerRepository.findById(followerId)).thenReturn(Optional.of(follower));

        assertDoesNotThrow(() -> {
            friendshipService.removeFollower(followerId);
        });

        verify(followerRepository).findById(followerId);
        verify(followerRepository).delete(follower);
    }

    @Test
    @DisplayName("Попытка удаления несуществующей подписки. Должна вызвать исключение")
    public void testRemoveNonExistentFollower() {
        Long followerId = 1L;

        when(followerRepository.findById(followerId)).thenReturn(Optional.empty());

        assertThrows(RequestNotFoundException.class, () -> {
            friendshipService.removeFollower(followerId);
        });

        verify(followerRepository).findById(followerId);
    }



    @Test
    @DisplayName("Получение списка дружб пользователя. Должно пройти успешно.")
    public void testGetFriendshipsForUserSuccess() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Friendship friendship1 = new Friendship();
        friendship1.setUserOne(user);
        friendship1.setUserTwo(new User());

        Friendship friendship2 = new Friendship();
        friendship2.setUserOne(new User());
        friendship2.setUserTwo(user);

        List<Friendship> friendships = Arrays.asList(friendship1, friendship2);

        when(friendshipRepository.findByUserOneIdOrUserTwoId(userId, userId)).thenReturn(friendships);

        List<FriendshipResponseDto> result = friendshipService.getFriendshipsForUser(userId);

        assertNotNull(result);
        assertEquals(friendships.size(), result.size());

        verify(friendshipRepository).findByUserOneIdOrUserTwoId(userId, userId);
    }
}
