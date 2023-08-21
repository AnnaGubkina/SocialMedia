package api.socialmedia.controller;


import api.socialmedia.dto.responce.PostResponseDto;
import api.socialmedia.service.PostFeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/posts/feed")
@Tag(name = "Post feed controller", description = "Feed operations")
@RequiredArgsConstructor
@Slf4j
public class PostFeedController {

    private final PostFeedService activityService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user activity feed", description = "Retrieve user's activity feed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activity feed retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<PostResponseDto>> getUserActivityFeed(
            @PathVariable Long userId,
            @Parameter(hidden = true) final Pageable pageable) {
        List<PostResponseDto> activityFeed = activityService.getUserActivityFeed(userId, pageable);
        return ResponseEntity.ok(activityFeed);
    }
}
