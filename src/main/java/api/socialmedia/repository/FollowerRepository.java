package api.socialmedia.repository;

import api.socialmedia.entity.Follower;
import api.socialmedia.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;



public interface FollowerRepository extends JpaRepository<Follower, Long> {

    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

    void deleteBySenderAndReceiver(User one, User one1);

    boolean existsByReceiverIdAndSenderId(Long receiverId, Long senderId);
}
