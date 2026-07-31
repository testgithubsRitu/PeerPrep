package com.peerprep.repository;

import com.peerprep.model.Session;
import com.peerprep.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByRequesterOrPartnerOrderByScheduledTimeDesc(User requester, User partner);
    List<Session> findByPartnerAndStatus(User partner, Session.Status status);
}
