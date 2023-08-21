package api.socialmedia.service.impl;

import api.socialmedia.dto.request.PostRequestDto;
import api.socialmedia.dto.responce.PostResponseDto;
import api.socialmedia.entity.Post;
import api.socialmedia.exception.AccessDeniedException;
import api.socialmedia.exception.PostNotFoundException;
import api.socialmedia.mapper.PostMapper;
import api.socialmedia.repository.PostRepository;
import api.socialmedia.service.PostService;
import api.socialmedia.util.AccessTokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {


    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final AccessTokenManager tokenManager;
    private final String FILE_URL = "http://localhost:8080/file/download/";


    /**
     * Создание поста.
     * Мы создаем сущность для сохранения в базу,
     * добавляем недостающие данные такие как дата и время, id пользователя.
     * Также добавляем в сущность файловые данные.
     * В response будем возвращать готовый пост, но без файловых данных,
     * вместо них прикрепим ссылку на запрос к файлу.
     */

    @Transactional
    public PostResponseDto createPost(PostRequestDto request, MultipartFile file) {
        Long userId = tokenManager.getUserId();
        Post post = postMapper.postRequestDtoToPostEntity(request);
        post.setUserId(userId);
        post.setDate(LocalDateTime.now());
        try {
            post.setFileData(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Post savePost = postRepository.save(post);
        String fileUrl = FILE_URL + savePost.getId();
        PostResponseDto responseDto = postMapper.postEntityToPostResponseDto(savePost);
        responseDto.setFileUrl(fileUrl);
        log.info("Post saved: {}", savePost.getTitle());
        return responseDto;
    }


    /**
     * Получение поста по ID
     * Реализация состоит в том, что мы получаем сущность из БД с файлом по Id поста,
     * но в response будем отдавать не сам файл, а ссылку на запрос к нему.
     */
    @Transactional(readOnly = true)
    public PostResponseDto findById(Long id) {
        Post post = postRepository.getPostById(id);
        if (post == null) {
            log.info("There's no post with id={}", id);
            throw new PostNotFoundException("There's no post with id=" + id);
        }
        PostResponseDto responseDto = postMapper.postEntityToPostResponseDto(post);
        String fileUrl = FILE_URL + post.getId();
        responseDto.setFileUrl(fileUrl);
        log.info("Post successfully received: {}", responseDto);
        return responseDto;
    }


    /**
     * Получение всех постов всех пользователей
     * Если сортировка не задана пользователем, то выводим сортировку по умолчанию, по дате в порядке убывания.
     */
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getAllPosts(Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "date"));
        }
        Page<Post> posts = postRepository.findAll(pageable);
        if (posts.getSize() == 0) {
            log.info("No posts in Database");
            throw new PostNotFoundException("No posts in Database");
        }
        log.info("All posts received: {}", posts);
        Page<PostResponseDto> listDto = posts.map(postMapper::postEntityToPostResponseDto);
        listDto.forEach(responseDto -> responseDto.setFileUrl(FILE_URL + responseDto.getId()));

        return listDto;
    }

    /**
     * Получение всех постов пользователя по его id
     * Если сортировка не задана, то выводим сортировку по умолчанию - по дате в порядке убывания.
     */
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getAllUsersPosts(Pageable pageable, Long userId) {
        if (!pageable.getSort().isSorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "date"));
        }
        Page<Post> posts = postRepository.findAllByUserId(userId, pageable);
        if (posts.getSize() == 0) {
            log.info("No posts in Database");
            throw new PostNotFoundException("No posts in Database");
        }
        log.info("List all user's posts with id : {} , {}", userId, posts);
        Page<PostResponseDto> listDto = posts.map(postMapper::postEntityToPostResponseDto);
        listDto.forEach(responseDto -> responseDto.setFileUrl(FILE_URL + responseDto.getId()));
        return listDto;
    }

    /**
     * Изменение своего поста по Id.
     * Редактировать пост может только пользователь, кому он принадлежит.
     * Редактировать можно только название и текст поста.
     */
    @Transactional
    public PostResponseDto updatePost(Long id, PostRequestDto editRequest) {
        Long userId = tokenManager.getUserId();
        Post postForUpdate = postRepository.findById(id).orElseThrow(() ->
                new PostNotFoundException(String.format("Post with id  %s not found in database", id)));
        log.info("Post from DB for update: {} ", postForUpdate.getTitle());
        if (Objects.equals(userId, postForUpdate.getUserId())) {
            postForUpdate.setTitle(editRequest.getTitle());
            postForUpdate.setText(editRequest.getText());
            postForUpdate.setDate(LocalDateTime.now());
            Post updatedPost = postRepository.save(postForUpdate);
            String fileUrl = FILE_URL + updatedPost.getId();
            PostResponseDto responseDto = postMapper.postEntityToPostResponseDto(updatedPost);
            responseDto.setFileUrl(fileUrl);
            log.info("Post updated: {}", responseDto.getTitle());
            return responseDto;
        } else {
            log.error("Editing someone else's post is not allowed");
            throw new AccessDeniedException("Editing someone else's post is not allowed");
        }
    }


    /**
     * Удаление своего поста по Id.
     * Удалять пост может только пользователь, кому он принадлежит.
     */

    @Transactional
    public void deletePost(Long id) {
        Long userId = tokenManager.getUserId();
        Post postForDelete = postRepository.findById(id).orElseThrow(() ->
                new PostNotFoundException(String.format("Post with id  %s not found in database", id)));
        if (Objects.equals(userId, postForDelete.getUserId())) {
            postRepository.deleteById(id);
        } else {
            log.error("Deleting someone else's post is not allowed");
            throw new AccessDeniedException("Deleting someone else's post is not allowed");
        }
    }
}
