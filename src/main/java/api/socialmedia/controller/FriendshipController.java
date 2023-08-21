package api.socialmedia.controller;


import api.socialmedia.dto.request.FriendshipRequestDto;
import api.socialmedia.dto.responce.FollowersResponseDto;
import api.socialmedia.dto.responce.FriendshipResponseDto;
import api.socialmedia.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Post request processing controller
 */
@RestController
@RequestMapping(value = "/friendships")
@Tag(name = "Friendship Controller", description = "Operations with friends")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;


    @Operation(summary = "Send a new friend request", responses = {
            @ApiResponse(responseCode = "201",
                    content = @Content(schema = @Schema(implementation = FollowersResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Request to yourself",
                    content = @Content)
    })
    @PostMapping("/request")
    public ResponseEntity<FollowersResponseDto> sendFriendshipRequest(@RequestBody FriendshipRequestDto request) {
        FollowersResponseDto responseDto = friendshipService.sendFriendshipRequest(request);
        log.info("Response with new friend request: {}",responseDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Accept a new friend request", responses = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = FriendshipResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Request for friendship not found",
                    content = @Content)
    })

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<FriendshipResponseDto> acceptFriendshipRequest(@PathVariable Long requestId) {
        FriendshipResponseDto responseDto = friendshipService.acceptFriendshipRequest(requestId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @Operation(summary = "Delete friendship",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Friendship deleted",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Friendship not found",
                            content = @Content)
            })
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFriendship(@RequestParam Long removerId, @RequestParam Long deletedId) {
        friendshipService.removeFriendship(removerId, deletedId);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    @Operation(summary = "Delete follower",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Follower deleted",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Follower not found",
                            content = @Content)
            })
    @DeleteMapping("/remove/{followerId}/follower")
    public ResponseEntity<?> removeFriendshipRequest(@PathVariable Long followerId) {
        friendshipService.removeFollower(followerId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @Operation(summary = "Get a list of all user's friendships",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Friendship deleted",
                            content = @Content),
            })
    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<FriendshipResponseDto>> getFriendshipsForUser(@PathVariable Long userId) {
        List<FriendshipResponseDto> friendships = friendshipService.getFriendshipsForUser(userId);
        return ResponseEntity.ok(friendships);
    }
}
