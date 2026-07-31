package com.peerprep.controller;

import com.peerprep.dto.SessionDtos.CreateSessionRequest;
import com.peerprep.dto.SessionDtos.FeedbackRequest;
import com.peerprep.exception.ResourceNotFoundException;
import com.peerprep.model.Feedback;
import com.peerprep.model.Session;
import com.peerprep.model.User;
import com.peerprep.repository.UserRepository;
import com.peerprep.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    public SessionController(SessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Session request(Authentication auth, @Valid @RequestBody CreateSessionRequest req) {
        return sessionService.request(currentUser(auth), req);
    }

    @GetMapping
    public List<Session> mySessions(Authentication auth) {
        return sessionService.mySessions(currentUser(auth));
    }

    @PostMapping("/{id}/accept")
    public Session accept(Authentication auth, @PathVariable Long id) {
        return sessionService.respond(currentUser(auth), id, true);
    }

    @PostMapping("/{id}/decline")
    public Session decline(Authentication auth, @PathVariable Long id) {
        return sessionService.respond(currentUser(auth), id, false);
    }

    @PostMapping("/{id}/complete")
    public Session complete(Authentication auth, @PathVariable Long id) {
        return sessionService.complete(currentUser(auth), id);
    }

    @PostMapping("/{id}/feedback")
    public Feedback feedback(Authentication auth, @PathVariable Long id, @Valid @RequestBody FeedbackRequest req) {
        return sessionService.leaveFeedback(currentUser(auth), id, req);
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
