package api.socialmedia.repository;

import api.socialmedia.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByDateAsc(Long userOneId, Long userTwoId, Long userTwoId1, Long userOneId1);
}