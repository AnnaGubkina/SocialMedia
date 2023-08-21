package api.socialmedia.controller;


import api.socialmedia.dto.request.PostRequestDto;
import api.socialmedia.dto.responce.PostResponseDto;
import api.socialmedia.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Post request processing controller
 */
@RestController
@RequestMapping(value = "/posts")
@Tag(name = "Post Controller", description = "REST operations with posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    /**
     * Create a post
     */
    @Operation(summary = "Create a new post", responses = {
            @ApiResponse(responseCode = "201", description = "Post created",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content)
    })
    @PostMapping(value = "/create")
    public ResponseEntity<PostResponseDto> createPost(@Valid @RequestPart("data") PostRequestDto request,
                                             @RequestPart("file") MultipartFile file) {
        PostResponseDto response = postService.createPost(request, file);
        log.info("Response with created post: {}", response.getTitle());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    /**
     * Get post by id.
     */
    @Operation(summary = "Get Post by post's ID", responses = {
            @ApiResponse(responseCode = "200", description = "Found post",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<PostResponseDto> getById(@PathVariable Long id) {
        PostResponseDto response = postService.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Get a list of posts.
     * @param pageable contains page number, how many results on page.
     * @return page with posts
     */
    @Operation(summary = "Get all posts", responses = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid pageable",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Posts not found",
                    content = @Content)
    })
    @GetMapping
    @PageableAsQueryParam
    public ResponseEntity<Page<PostResponseDto>> getAllPosts(@Parameter(hidden = true) final Pageable pageable) {
        Page<PostResponseDto> posts = postService.getAllPosts(pageable);
        log.info(String.format("Posts %s received successfully", posts));
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }



    /**
     * Get a list of user's posts by id.
     * @param pageable contains page number, how many results on page.
     * @return page with current user's posts
     */
    @Operation(summary = "Get all user's posts", responses = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid pageable",
                    content = @Content)
    })
    @GetMapping(value = "/{id}/user")
    @PageableAsQueryParam
    public ResponseEntity<Page<PostResponseDto>> getAllUsersPosts(@Parameter(hidden = true) final Pageable pageable,
                                                                  @PathVariable Long id) {
        Page<PostResponseDto> posts = postService.getAllUsersPosts(pageable, id);
        log.info(String.format("User's posts %s received successfully ", posts));
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }




    /**
     * Edit post.
     * @param editRequest contains required parameters 'title' and 'text'
     * @param id of post
     * @return updated post
     */
    @Operation(summary = "Post update",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Post updated",
                            content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Post not found",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access to someone else's post is prohibited",
                            content = @Content)
            })
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> updatePost(@Valid @RequestBody PostRequestDto editRequest, @PathVariable Long id) {
        PostResponseDto updatedPost = postService.updatePost(id, editRequest);
        log.info("Post updated successfully");
        return new ResponseEntity<>(updatedPost, HttpStatus.OK);
    }


    /**
     * Delete post.
     * @param id of post
     */
    @Operation(summary = "Delete post",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Post deleted",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Post not found",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access to someone else's post is prohibited",
                            content = @Content)
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost( @PathVariable Long id) {
        postService.deletePost(id);
        log.info("Post with id {} deleted successfully", id);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
