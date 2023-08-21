package api.socialmedia.service;

import api.socialmedia.entity.Post;
import api.socialmedia.exception.PostNotFoundException;
import api.socialmedia.repository.PostRepository;
import api.socialmedia.service.impl.FileServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing File service functionality.")
public class FileServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    @DisplayName("Загрузка файла. Должно пройти успешно.")
    public void testDownloadFileSuccess() {
        Long postId = 1L;
        byte[] fileData = new byte[]{1, 2, 3, 4};
        Post post = new Post();
        post.setId(postId);
        post.setFileData(fileData);

        when(postRepository.getPostById(postId)).thenReturn(post);

        byte[] result = fileService.downloadFile(postId);

        assertNotNull(result);
        assertArrayEquals(fileData, result);
        verify(postRepository).getPostById(postId);
    }

    @Test
    @DisplayName("Неудачная загрузка файла. Должно выбросить исключение.")
    public void testDownloadFile_PostNotFoundException() {
        Long postId = 1L;
        when(postRepository.getPostById(postId)).thenReturn(null);
        assertThrows(PostNotFoundException.class, () -> fileService.downloadFile(postId));
        verify(postRepository).getPostById(postId);
    }
}


