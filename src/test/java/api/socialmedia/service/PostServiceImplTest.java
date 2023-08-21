package api.socialmedia.service;

import api.socialmedia.dto.request.PostRequestDto;
import api.socialmedia.dto.responce.PostResponseDto;
import api.socialmedia.entity.Post;
import api.socialmedia.exception.AccessDeniedException;
import api.socialmedia.exception.PostNotFoundException;
import api.socialmedia.mapper.PostMapper;
import api.socialmedia.repository.PostRepository;
import api.socialmedia.service.impl.PostServiceImpl;
import api.socialmedia.util.AccessTokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing Post service functionality.")
public class PostServiceImplTest {

    @InjectMocks
    private PostServiceImpl postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private AccessTokenManager tokenManager;

    public static final MockMultipartFile MOCK_MULTIPART_FILE
            = new MockMultipartFile(
            "file",
            "hello.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
    );

    private static final String FILE_URL = "http://localhost:8080/file/download/1";


    @Test
    @DisplayName("Создать пост. Должно пройти успешно.")
    public void testCreatePost() throws IOException {
        // мокаем входящие данные
        Long userId = 1L;
        PostRequestDto requestDto = new PostRequestDto("post_one", "text*test*text");
        MultipartFile file = MOCK_MULTIPART_FILE;
        LocalDateTime date = LocalDateTime.now();
        PostResponseDto expectedResponseDto = new PostResponseDto(1L, "post_one", "text*test*text", FILE_URL, userId, date);

        // мокаем зависимости

        Post post = new Post(1L, "post_one", "text*test*text", file.getBytes(), userId, date);
        when(postMapper.postRequestDtoToPostEntity(requestDto)).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.postEntityToPostResponseDto(post)).thenReturn(expectedResponseDto);

        // тестируем
        PostResponseDto responseDto = postService.createPost(requestDto, file);
        assertEquals(userId, responseDto.getUserId());
        assertEquals(requestDto.getText(), responseDto.getText());

    }

    @Test
    @DisplayName("Создать пост. Ошибка ввода данных 400.")
    public void testCreatePost_inputDataException400() throws IOException {
        // Входящие данные с неверным заголовком
        PostRequestDto requestDto = new PostRequestDto("", "Some text here");
        MultipartFile file = MOCK_MULTIPART_FILE;

        assertThrows(RuntimeException.class, () -> postService.createPost(requestDto, file));
    }


    @Test
    @DisplayName("Получить пост по ID. Должен пройти успешно")
    public void testFindByIdValidPost() {
        Long postId = 1L;
        Post post = new Post(postId, "Title", "Text", new byte[0], 1L, LocalDateTime.now());

        //мокируем репозиторий
        when(postRepository.getPostById(postId)).thenReturn(post);

        //мокируем маппер
        PostResponseDto responseDto = new PostResponseDto(post.getId(), post.getTitle(), post.getText(), FILE_URL + post.getId(), post.getUserId(), post.getDate());
        when(postMapper.postEntityToPostResponseDto(post)).thenReturn(responseDto);

        PostResponseDto result = postService.findById(postId);

        assertEquals(responseDto, result);
    }

    @Test
    @DisplayName("Получить пост по ID. Возврат null - PostNotFoundException")
    public void testFindByIdNonexistentPost_PostNotFoundException404() {
        Long postId = 1L;
        when(postRepository.getPostById(postId)).thenReturn(null);
        //ожидаем ошибку
        assertThrows(PostNotFoundException.class, () -> postService.findById(postId));
    }

    @Test
    @DisplayName("Изменение поста. Должно пройти успешно")
    public void testUpdatePostSuccess() {
        Long userId = 1L;
        Long postId = 1L;

        PostRequestDto editRequest = new PostRequestDto("Updated Title", "Updated Text");
        Post postFromDB = new Post(postId, "Title", "Text", new byte[0], userId, LocalDateTime.now());

        when(tokenManager.getUserId()).thenReturn(userId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(postFromDB));

        Post updatedPost = new Post(postId, editRequest.getTitle(), editRequest.getText(), new byte[0], userId, LocalDateTime.now());
        when(postRepository.save(any())).thenReturn(updatedPost);

        when(postMapper.postEntityToPostResponseDto(updatedPost)).thenReturn(
                new PostResponseDto(postId, editRequest.getTitle(), editRequest.getText(),
                        FILE_URL + postId, userId, LocalDateTime.now())
        );

        PostResponseDto result = postService.updatePost(postId, editRequest);
        assertNotNull(result);
        assertEquals(editRequest.getTitle(), result.getTitle());
        assertEquals(editRequest.getText(), result.getText());
        assertEquals(FILE_URL, result.getFileUrl());
        assertEquals(userId, result.getUserId());

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).save(any());
    }

    @Test()
    @DisplayName("Изменение поста. Ошибка доступа при попытке редактирования чужого поста")
    public void testUpdatePost_AccessDeniedException403() {
        Long deniedUserId = 1L;
        Long userId = 2L;
        Long postId = 1L;

        PostRequestDto editRequest = new PostRequestDto("Updated Title", "Updated Text");
        Post postFromDB = new Post(postId, "Title", "Text", new byte[0], userId, LocalDateTime.now());

        when(tokenManager.getUserId()).thenReturn(deniedUserId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(postFromDB));

        assertThrows(AccessDeniedException.class, () -> postService.updatePost(postId, editRequest));
    }

    @Test()
    @DisplayName("Изменение поста. Пост для редактирования не найден в базе данных")
    public void testUpdate_PostNotFoundException404() {
        Long postId = 1L;
        PostRequestDto editRequest = new PostRequestDto("Updated Title", "Updated Text");

        when(tokenManager.getUserId()).thenReturn(1L);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.updatePost(postId, editRequest));
    }


    @Test
    @DisplayName("Удаление поста. Должно пройти успешно")
    public void testDeletePostSuccess() {
        Long userId = 1L;
        Long postId = 1L;

        Post postFromDB = new Post(postId, "Title", "Text", new byte[0], userId, LocalDateTime.now());

        when(tokenManager.getUserId()).thenReturn(userId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(postFromDB));

        postService.deletePost(postId);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).deleteById(postId);
    }

    @Test()
    @DisplayName("Удаление поста. Ошибка доступа при попытке удаления чужого поста")
    public void testDeletePost_AccessDeniedException403() {
        Long userId = 1L;
        Long postId = 1L;

        Post postFromDB = new Post(postId, "Title", "Text", new byte[0], userId + 1, LocalDateTime.now());

        when(tokenManager.getUserId()).thenReturn(userId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(postFromDB));


        assertThrows(AccessDeniedException.class, () -> postService.deletePost(postId));
    }

    @Test()
    @DisplayName("Удаление поста. Пост для удаления не найден в базе данных")
    public void testDeletePost_PostNotFoundException404() {
        Long postId = 1L;
        when(tokenManager.getUserId()).thenReturn(1L);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> postService.deletePost(postId));
    }


    @Test
    @DisplayName("Получить все посты всех пользователей. Должен пройти успешно")
    public void testGetAllPosts() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "date"));

        List<Post> mockPosts = new ArrayList<>();
        mockPosts.add(new Post(1L, "Title 1", "Text 1", new byte[0], 1L, LocalDateTime.now()));
        mockPosts.add(new Post(2L, "Title 2", "Text 2", new byte[0], 2L, LocalDateTime.now()));

        // мокируем репозиторий
        Page<Post> mockPage = new PageImpl<>(mockPosts);
        when(postRepository.findAll(pageable)).thenReturn(mockPage);

        // мокируем маппер
        PostResponseDto responseDto1 = new PostResponseDto(1L, "Title 1", "Text 1", FILE_URL + 1L, 1L, LocalDateTime.now());
        PostResponseDto responseDto2 = new PostResponseDto(2L, "Title 2", "Text 2", FILE_URL + 2L, 2L, LocalDateTime.now());
        when(postMapper.postEntityToPostResponseDto(mockPosts.get(0))).thenReturn(responseDto1);
        when(postMapper.postEntityToPostResponseDto(mockPosts.get(1))).thenReturn(responseDto2);

        Page<PostResponseDto> result = postService.getAllPosts(pageable);

        assertEquals(mockPage.getTotalElements(), result.getTotalElements());
        assertEquals(responseDto1, result.getContent().get(0));
        assertEquals(responseDto2, result.getContent().get(1));
    }

    @Test
    @DisplayName("Получить все посты всех пользователей. Пустой Page объект без постов")
    public void testGetAllPostsEmptyPage_PostNotFoundException404() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "date"));

        // мокируем репозиторий, он должен вернуть пустой Page
        Page<Post> emptyPage = new PageImpl<>(Collections.emptyList());
        when(postRepository.findAll(pageable)).thenReturn(emptyPage);

        assertThrows(PostNotFoundException.class, () -> postService.getAllPosts(pageable));
    }


    @Test
    @DisplayName("Получить все посты пользователя по ID. Должен пройти успешно")
    public void testGetAllUsersPosts() {
        int pageNumber = 0;
        int pageSize = 10;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "date"));

        List<Post> mockPosts = new ArrayList<>();
        mockPosts.add(new Post(1L, "Title 1", "Text 1", new byte[0], userId, LocalDateTime.now()));
        mockPosts.add(new Post(2L, "Title 2", "Text 2", new byte[0], userId, LocalDateTime.now()));

        // мокируем репозиторий
        Page<Post> mockPage = new PageImpl<>(mockPosts);
        when(postRepository.findAllByUserId(userId, pageable)).thenReturn(mockPage);

        // мокируем маппер, должен вернуть PostResponseDto
        PostResponseDto responseDto1 = new PostResponseDto(1L, "Title 1", "Text 1", FILE_URL + 1L, userId, LocalDateTime.now());
        PostResponseDto responseDto2 = new PostResponseDto(2L, "Title 2", "Text 2", FILE_URL + 2L, userId, LocalDateTime.now());
        when(postMapper.postEntityToPostResponseDto(mockPosts.get(0))).thenReturn(responseDto1);
        when(postMapper.postEntityToPostResponseDto(mockPosts.get(1))).thenReturn(responseDto2);

        Page<PostResponseDto> result = postService.getAllUsersPosts(pageable, userId);

        assertEquals(mockPage.getTotalElements(), result.getTotalElements());
        assertEquals(responseDto1, result.getContent().get(0));
        assertEquals(responseDto2, result.getContent().get(1));
    }

    @Test
    @DisplayName("Получить все посты пользователя по ID. Пустой Page объект без постов")
    public void testGetAllUsersPosts_PostNotFoundException404() {
        int pageNumber = 0;
        int pageSize = 10;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "date"));

        // мокируем репозиторий, он должен вернуть пустой Page
        Page<Post> emptyPage = new PageImpl<>(Collections.emptyList());
        when(postRepository.findAllByUserId(userId, pageable)).thenReturn(emptyPage);

        assertThrows(PostNotFoundException.class, () -> postService.getAllUsersPosts(pageable, userId));
    }
}


































