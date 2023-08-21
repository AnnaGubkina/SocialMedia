package api.socialmedia.service;

import api.socialmedia.dto.request.PostRequestDto;
import api.socialmedia.dto.responce.PostResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {


    PostResponseDto createPost(PostRequestDto request, MultipartFile file);

    PostResponseDto findById(Long id);

    Page<PostResponseDto> getAllPosts(Pageable pageable);

    Page<PostResponseDto> getAllUsersPosts(Pageable pageable, Long id);

    PostResponseDto updatePost(Long id, PostRequestDto editRequest);

    void deletePost(Long id);
}
