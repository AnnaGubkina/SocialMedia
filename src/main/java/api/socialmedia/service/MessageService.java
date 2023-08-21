package api.socialmedia.service;


import api.socialmedia.dto.request.MessageRequestDto;
import api.socialmedia.dto.responce.MessageResponseDto;

import java.util.List;

public interface MessageService {

    void sendMessage(MessageRequestDto messageRequestDto);

    List<MessageResponseDto> getMessages(Long userOneId, Long userTwoId);
}
