package api.socialmedia.service.impl;

import api.socialmedia.entity.Post;
import api.socialmedia.exception.PostNotFoundException;
import api.socialmedia.repository.PostRepository;
import api.socialmedia.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final PostRepository postRepository;

    /**
     * Загрузить файл по Id
     * @param id
     */

    @Override
    public byte[] downloadFile(Long id) {
        Post post = postRepository.getPostById(id);
        if (post == null) {
            log.info("Post not found");
            throw new PostNotFoundException("Post not found");
        }
        log.info("Post loaded successfully : {}", post);
        return post.getFileData();
    }
}
