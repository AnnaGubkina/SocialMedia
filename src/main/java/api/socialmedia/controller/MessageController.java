package api.socialmedia.controller;


import api.socialmedia.dto.request.MessageRequestDto;
import api.socialmedia.dto.responce.MessageResponseDto;
import api.socialmedia.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/messages")
@Tag(name = "Message Controller", description = "Operations with messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;



    @Operation(summary = "Send a message", description = "Send a message from sender to receiver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody MessageRequestDto messageRequestDto) {
        messageService.sendMessage(messageRequestDto);
        return ResponseEntity.ok("Message sent successfully.");
    }


    @Operation(summary = "Get messages between users", description = "Retrieve messages between two users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userOneId}/{userTwoId}")
    public ResponseEntity<List<MessageResponseDto>> getMessages(@PathVariable Long userOneId, @PathVariable Long userTwoId) {
        List<MessageResponseDto> messages = messageService.getMessages(userOneId, userTwoId);
        return ResponseEntity.ok(messages);
    }
}
