package api.socialmedia.service;


import api.socialmedia.dto.request.MessageRequestDto;
import api.socialmedia.dto.responce.MessageResponseDto;
import api.socialmedia.entity.Friendship;
import api.socialmedia.entity.Message;
import api.socialmedia.entity.User;
import api.socialmedia.exception.FriendshipNotFoundException;
import api.socialmedia.repository.FriendshipRepository;
import api.socialmedia.repository.MessageRepository;
import api.socialmedia.repository.UserRepository;
import api.socialmedia.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing Message service functionality.")
public class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private MessageServiceImpl messageService;


    @Test
    @DisplayName("Отправка сообщения. Должно пройти успешно.")
    public void testSendMessageSuccess() {
        Long senderId = 1L;
        Long receiverId = 2L;
        String text = "Привет, пошли в кино?";

        User sender = new User();
        sender.setId(senderId);

        User receiver = new User();
        receiver.setId(receiverId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(friendshipRepository.existsByUserOneIdAndUserTwoId(senderId, receiverId)).thenReturn(true);

        MessageRequestDto messageRequestDto = new MessageRequestDto();
        messageRequestDto.setSenderId(senderId);
        messageRequestDto.setReceiverId(receiverId);
        messageRequestDto.setText(text);

        messageService.sendMessage(messageRequestDto);

        verify(userRepository).findById(senderId);
        verify(userRepository).findById(receiverId);
        verify(friendshipRepository).existsByUserOneIdAndUserTwoId(senderId, receiverId);
    }

    @Test
    @DisplayName("Отправка сообщения неудачна из-за отсутствия дружбы. Должна выбросить исключение.")
    public void testSendMessage_FriendshipNotFoundException() {
        Long senderId = 1L;
        Long receiverId = 2L;
        String text = "Hello, how are you?";

        User sender = new User();
        sender.setId(senderId);

        User receiver = new User();
        receiver.setId(receiverId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(friendshipRepository.existsByUserOneIdAndUserTwoId(senderId, receiverId)).thenReturn(false);

        MessageRequestDto messageRequestDto = new MessageRequestDto();
        messageRequestDto.setSenderId(senderId);
        messageRequestDto.setReceiverId(receiverId);
        messageRequestDto.setText(text);

        assertThrows(FriendshipNotFoundException.class, () -> messageService.sendMessage(messageRequestDto));

        verify(userRepository).findById(senderId);
        verify(userRepository).findById(receiverId);
        verify(friendshipRepository).existsByUserOneIdAndUserTwoId(senderId, receiverId);
        verifyNoInteractions(messageRepository);
    }



    @Test
    @DisplayName("Получение списка сообщений между друзьями. Должно пройти успешно.")
    public void testGetMessagesSuccess() {
        Long userOneId = 1L;
        Long userTwoId = 2L;

        User userOne = new User();
        userOne.setId(userOneId);

        User userTwo = new User();
        userTwo.setId(userTwoId);

        Friendship friendship = new Friendship();
        friendship.setUserOne(userOne);
        friendship.setUserTwo(userTwo);

        Message message1 = new Message();
        message1.setId(1L);
        message1.setSender(userOne);
        message1.setReceiver(userTwo);
        message1.setText("Hello");
        message1.setDate(LocalDateTime.now());

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSender(userTwo);
        message2.setReceiver(userOne);
        message2.setText("Hi");
        message2.setDate(LocalDateTime.now());

        List<Message> messages = Arrays.asList(message1, message2);

        when(friendshipRepository.existsByUserOneIdAndUserTwoId(userOneId, userTwoId)).thenReturn(true);
        when(messageRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByDateAsc(userOneId, userTwoId, userTwoId, userOneId))
                .thenReturn(messages);

        List<MessageResponseDto> result = messageService.getMessages(userOneId, userTwoId);

        assertNotNull(result);
        assertEquals(messages.size(), result.size());

        verify(friendshipRepository).existsByUserOneIdAndUserTwoId(userOneId, userTwoId);
        verify(messageRepository).findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByDateAsc(userOneId, userTwoId, userTwoId, userOneId);
    }

    @Test
    @DisplayName("Получение списка сообщений неудачно из-за отсутствия дружбы. Должно выбросить исключение.")
    public void testGetMessages_FriendshipNotFoundException() {
        Long userOneId = 1L;
        Long userTwoId = 2L;

        when(friendshipRepository.existsByUserOneIdAndUserTwoId(userOneId, userTwoId)).thenReturn(false);

        assertThrows(FriendshipNotFoundException.class, () -> messageService.getMessages(userOneId, userTwoId));

        verify(friendshipRepository).existsByUserOneIdAndUserTwoId(userOneId, userTwoId);
        verifyNoInteractions(messageRepository);
    }

}



