package api.socialmedia.service.impl;

import api.socialmedia.dto.request.MessageRequestDto;
import api.socialmedia.dto.responce.MessageResponseDto;
import api.socialmedia.entity.Message;
import api.socialmedia.entity.User;
import api.socialmedia.exception.FriendshipNotFoundException;
import api.socialmedia.exception.UserNotFoundException;
import api.socialmedia.repository.FriendshipRepository;
import api.socialmedia.repository.MessageRepository;
import api.socialmedia.repository.UserRepository;
import api.socialmedia.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    /**
     * Отправка сообщения пользователю
     * Метод проверяет наличие обоих юзеров, являются ли они друзьями
     * и формирует сущность сообщения для сохранения в базу.
     * В сущности указаны отправитель, получатель, текст и дата сообщения.
     */

    @Transactional
    public void sendMessage(MessageRequestDto messageRequestDto) {
        User sender = userRepository.findById(messageRequestDto.getSenderId())
                .orElseThrow(() -> new UserNotFoundException("Sender user not found"));

        User receiver = userRepository.findById(messageRequestDto.getReceiverId())
                .orElseThrow(() -> new UserNotFoundException("Receiver user not found"));

        if (!areFriends(sender.getId(), receiver.getId())) {
            throw new FriendshipNotFoundException("Users are not friends");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setText(messageRequestDto.getText());
        message.setDate(LocalDateTime.now());

        messageRepository.save(message);
    }


    /**
     * Запрос переписки между двумя пользователями.
     * Проверяет являются ли пользователи друзьями.
     * Сообщения выводятся списком, по времени отправки
     * @param userOneId
     * @param userTwoId
     */
    @Transactional(readOnly = true)
    public List<MessageResponseDto> getMessages(Long userOneId, Long userTwoId) {
        if (!areFriends(userOneId, userTwoId)) {
            throw new FriendshipNotFoundException("Users are not friends");
        }
        List<Message> messages = messageRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByDateAsc(
                userOneId, userTwoId, userTwoId, userOneId
        );
        return messages.stream()
                .map(this::mapToMessageResponseDto)
                .collect(Collectors.toList());
    }


    /**
     * Проверяет, являются ли пользователи друзьями
     */
    @Transactional(readOnly = true)
    private boolean areFriends(Long UserOneId, Long UserTwoId) {
        return friendshipRepository.existsByUserOneIdAndUserTwoId(UserOneId, UserTwoId) ||
                friendshipRepository.existsByUserOneIdAndUserTwoId(UserTwoId, UserOneId);
    }


    private MessageResponseDto mapToMessageResponseDto(Message message) {
        MessageResponseDto dto = new MessageResponseDto();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setText(message.getText());
        dto.setDate(message.getDate());
        return dto;
    }
}
