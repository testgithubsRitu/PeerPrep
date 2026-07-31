package com.peerprep.repository;

import com.peerprep.model.Feedback;
import com.peerprep.model.Session;
import com.peerprep.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findBySession(Session session);
    List<Feedback> findByToUser(User toUser);
}
