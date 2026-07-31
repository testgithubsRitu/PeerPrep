package com.peerprep.service;

import com.peerprep.dto.SessionDtos.CreateSessionRequest;
import com.peerprep.dto.SessionDtos.FeedbackRequest;
import com.peerprep.exception.ResourceNotFoundException;
import com.peerprep.model.*;
import com.peerprep.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final FeedbackRepository feedbackRepository;

    public SessionService(SessionRepository sessionRepository, UserRepository userRepository,
                           SkillRepository skillRepository, FeedbackRepository feedbackRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public Session request(User requester, CreateSessionRequest req) {
        User partner = userRepository.findById(req.partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
        Skill skill = skillRepository.findByNameIgnoreCase(req.skillName)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown skill: " + req.skillName));

        if (partner.getId().equals(requester.getId())) {
            throw new IllegalArgumentException("You can't request a session with yourself");
        }

        Session session = new Session();
        session.setRequester(requester);
        session.setPartner(partner);
        session.setSkill(skill);
        session.setScheduledTime(req.scheduledTime);
        session.setStatus(Session.Status.PENDING);
        return sessionRepository.save(session);
    }

    public Session respond(User currentUser, Long sessionId, boolean accept) {
        Session session = getOwnedSession(currentUser, sessionId);
        if (!session.getPartner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only the invited partner can accept or decline");
        }
        session.setStatus(accept ? Session.Status.ACCEPTED : Session.Status.DECLINED);
        return sessionRepository.save(session);
    }

    public Session complete(User currentUser, Long sessionId) {
        Session session = getOwnedSession(currentUser, sessionId);
        if (session.getStatus() != Session.Status.ACCEPTED) {
            throw new IllegalArgumentException("Only accepted sessions can be marked complete");
        }
        session.setStatus(Session.Status.COMPLETED);
        sessionRepository.save(session);

        updateStreakAndCount(session.getRequester());
        updateStreakAndCount(session.getPartner());
        return session;
    }

    private void updateStreakAndCount(User user) {
        LocalDate today = LocalDate.now();
        LocalDate last = user.getLastSessionDate();

        if (last == null || last.isBefore(today.minusDays(1))) {
            user.setCurrentStreak(1);
        } else if (last.equals(today.minusDays(1))) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        }
        // if last == today, streak already counted for today - leave as is

        user.setLongestStreak(Math.max(user.getLongestStreak(), user.getCurrentStreak()));
        user.setLastSessionDate(today);
        user.setSessionsCompleted(user.getSessionsCompleted() + 1);
        userRepository.save(user);
    }

    public Feedback leaveFeedback(User currentUser, Long sessionId, FeedbackRequest req) {
        Session session = getOwnedSession(currentUser, sessionId);
        if (session.getStatus() != Session.Status.COMPLETED) {
            throw new IllegalArgumentException("Feedback can only be left on completed sessions");
        }
        if (req.rating < 1 || req.rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        User toUser = session.getRequester().getId().equals(currentUser.getId())
                ? session.getPartner() : session.getRequester();

        Feedback feedback = new Feedback();
        feedback.setSession(session);
        feedback.setFromUser(currentUser);
        feedback.setToUser(toUser);
        feedback.setRating(req.rating);
        feedback.setComment(req.comment);
        feedback = feedbackRepository.save(feedback);

        recomputeAverageRating(toUser);
        return feedback;
    }

    private void recomputeAverageRating(User user) {
        List<Feedback> allFeedbackForUser = feedbackRepository.findByToUser(user);
        double avg = allFeedbackForUser.stream().mapToInt(Feedback::getRating).average().orElse(0.0);
        user.setAverageRating(Math.round(avg * 10.0) / 10.0);
        user.setRatingCount(allFeedbackForUser.size());
        userRepository.save(user);
    }

    public List<Session> mySessions(User user) {
        return sessionRepository.findByRequesterOrPartnerOrderByScheduledTimeDesc(user, user);
    }

    private Session getOwnedSession(User user, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        boolean owns = session.getRequester().getId().equals(user.getId())
                || session.getPartner().getId().equals(user.getId());
        if (!owns) {
            throw new IllegalArgumentException("You don't have access to this session");
        }
        return session;
    }
}
