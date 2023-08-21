package api.socialmedia.repository;

import api.socialmedia.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {


    Post getPostById(Long userId);

    Page<Post> findAllByUserId(Long userId, Pageable pageable);

    Page<Post> findByUserIdIn(List<Long> friendsIds, Pageable pageable);
}


