package api.socialmedia.service.impl;

import api.socialmedia.dto.request.FriendshipRequestDto;
import api.socialmedia.dto.responce.FollowersResponseDto;
import api.socialmedia.dto.responce.FriendshipResponseDto;
import api.socialmedia.entity.Follower;
import api.socialmedia.entity.Friendship;
import api.socialmedia.exception.AccessDeniedException;
import api.socialmedia.exception.DuplicateFriendshipRequestException;
import api.socialmedia.exception.FriendshipNotFoundException;
import api.socialmedia.exception.RequestNotFoundException;
import api.socialmedia.repository.FollowerRepository;
import api.socialmedia.repository.FriendshipRepository;
import api.socialmedia.repository.UserRepository;
import api.socialmedia.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {


    private final FriendshipRepository friendshipRepository;
    private final FollowerRepository followerRepository;
    private final UserRepository userRepository;

    /**
     * Отправка запроса на добавление в друзья.
     * Реализация:
     * Из запроса получаем id отправителя и получателя и проверяем не равны ли они.
     * Проверяем не было ли такого уже запроса отправлено(чтобы избежать дублей в базе)
     * Сохраняем наш запрос в таблицу followers
     */

    @Transactional
    public FollowersResponseDto sendFriendshipRequest(FriendshipRequestDto request) {
        Long senderId = request.getSenderId();
        Long receiverId = request.getReceiverId();
        if (Objects.equals(senderId, receiverId)) {
            throw new AccessDeniedException("You can't send a friend request to yourself");
        }

        if (followerRepository.existsBySenderIdAndReceiverId(senderId, receiverId)) {
            throw new DuplicateFriendshipRequestException("Duplicate friendship request");
        }

        Follower followerRequest = saveFollower(senderId, receiverId);
        log.info("FollowerRequest successfully created: {}", followerRequest);
        return mapToFollowersResponseDto(followerRequest);
    }


    /**
     * Подтверждение запроса на добавление в друзья по id запроса.
     * Реализация:
     * Получаем запрос из базы, чтобы проверить, существует ли он.
     * Проверяем не было ли такого уже запроса отправлено(чтобы избежать дублей в базе)
     * Делаем проверку не существует ли уже такой дружбы(чтобы не было дублей дружб в базе)
     * Сохраняем новую запись о дружбе в базу и
     * делаем запись о втором подписчике(если он еще не существует, возможно такое что они оба сделали запросы друг другу)
     */

    @Transactional
    public FriendshipResponseDto acceptFriendshipRequest(Long requestId) {
        Follower followerFromDB = followerRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Request for friendship not found"));
        Long senderId = followerFromDB.getSender().getId();
        Long receiverId = followerFromDB.getReceiver().getId();

        if (friendshipRepository.existsByUserOneIdAndUserTwoId(senderId, receiverId) ||
                friendshipRepository.existsByUserOneIdAndUserTwoId(receiverId, senderId)) {
            throw new DuplicateFriendshipRequestException("Duplicate friendship request");
        }

        Friendship newFriendship = saveFriendship(followerFromDB);
        log.info("Friendship successfully created: {}", newFriendship);

        if (!(followerRepository.existsByReceiverIdAndSenderId(senderId, receiverId))) {
            Follower followerRequest = saveFollowerTwo(senderId, receiverId);
            log.info("FollowerRequest successfully created: {}", followerRequest);
        }
        return mapToFriendshipResponseDto(newFriendship);
    }


    /**
     * Удаление дружбы по id удаляющего и удаляемого.
     * Реализация:
     * Получаем дружбу из базы по ID юзеров. Так как запросы друг другу могли отправить как первый так и второй юзер,
     * выполняется двойная проверка.
     * Удаляем дружбу.
     * Удаляем подписку удаляющего пользователя на того кого он удалил
     * Удаленный остается подписанным на удаляющего
     */
    @Transactional
    public void removeFriendship(Long removerId, Long deletedId) {
        Optional<Friendship> friendshipFromDB = friendshipRepository.findByUserOneIdAndUserTwoId(removerId, deletedId);
        if (friendshipFromDB.isEmpty()) {
            friendshipFromDB = friendshipRepository.findByUserTwoIdAndUserOneId(removerId, deletedId);
            if (friendshipFromDB.isEmpty()) {
                throw new FriendshipNotFoundException("Friendship not found");
            }
        }
        Friendship friendship = friendshipFromDB.get();
        friendshipRepository.delete(friendship);
        log.info("User with id: {} removed user with id: {}", removerId, deletedId);

        followerRepository.deleteBySenderAndReceiver(
                userRepository.getById(removerId),
                userRepository.getById(deletedId));
    }

    /**
     * Удаление подписки на другого пользователя по Id подписки.
     */
    @Transactional
    public void removeFollower(Long followerId) {
        Follower followerFromDB = followerRepository.findById(followerId)
                .orElseThrow(() -> new RequestNotFoundException("Request for friendship not found"));
        followerRepository.delete(followerFromDB);
    }

    /**
     * Получение списка всех записей о дружбах пользователя по Id.
     */
    @Transactional(readOnly = true)
    public List<FriendshipResponseDto> getFriendshipsForUser(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByUserOneIdOrUserTwoId(userId, userId);
        return friendships.stream()
                .map(this::mapToFriendshipResponseDto)
                .collect(Collectors.toList());
    }


    private Friendship saveFriendship(Follower followerFromDB) {
        Friendship friendshipRequest = new Friendship();
        friendshipRequest.setUserOne(followerFromDB.getSender());
        friendshipRequest.setUserTwo(followerFromDB.getReceiver());
        friendshipRequest.setDate(LocalDateTime.now());
        Friendship newFriendship = friendshipRepository.save(friendshipRequest);
        log.info("Friendship saved: {}", newFriendship);
        return newFriendship;
    }

    private Follower saveFollower(Long senderId, Long receiverId) {
        Follower follower = new Follower();
        follower.setSender(userRepository.getById(senderId));
        follower.setReceiver(userRepository.getById(receiverId));
        follower.setDate(LocalDateTime.now());
        Follower savedFollower = followerRepository.save(follower);
        log.info("Follower saved: {}", savedFollower);
        return savedFollower;
    }

    private Follower saveFollowerTwo(Long senderId, Long receiverId) {
        Follower followerTwo = new Follower();
        followerTwo.setSender(userRepository.getById(receiverId));
        followerTwo.setReceiver(userRepository.getById(senderId));
        followerTwo.setDate(LocalDateTime.now());
        Follower savedFollower = followerRepository.save(followerTwo);
        log.info("Follower saved: {}", savedFollower);
        return savedFollower;
    }


    public FriendshipResponseDto mapToFriendshipResponseDto(Friendship friendship) {
        FriendshipResponseDto dto = new FriendshipResponseDto();
        dto.setId(friendship.getId());
        dto.setUserOneId(friendship.getUserOne().getId());
        dto.setUserTwoId(friendship.getUserTwo().getId());
        dto.setDate(friendship.getDate());
        return dto;
    }

    private FollowersResponseDto mapToFollowersResponseDto(Follower request) {
        FollowersResponseDto dto = new FollowersResponseDto();
        dto.setId(request.getId());
        dto.setSenderId(request.getSender().getId());
        dto.setReceiverId(request.getReceiver().getId());
        dto.setDate(request.getDate());
        return dto;
    }
}
