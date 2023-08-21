package api.socialmedia.repository;

import api.socialmedia.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {


    boolean existsByUserOneIdAndUserTwoId(Long senderId, Long receiverId);

    Optional<Friendship> findByUserOneIdAndUserTwoId(Long removerId, Long deletedId);

    Optional<Friendship> findByUserTwoIdAndUserOneId(Long deletedId, Long removerId);

    List<Friendship> findByUserOneIdOrUserTwoId(Long userOneId, Long userTwoId);



}