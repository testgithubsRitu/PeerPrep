package com.peerprep.controller;

import com.peerprep.dto.SkillDtos.MatchSuggestion;
import com.peerprep.exception.ResourceNotFoundException;
import com.peerprep.model.User;
import com.peerprep.repository.UserRepository;
import com.peerprep.service.MatchingService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchingService matchingService;
    private final UserRepository userRepository;

    public MatchController(MatchingService matchingService, UserRepository userRepository) {
        this.matchingService = matchingService;
        this.userRepository = userRepository;
    }

    // GET /api/matches?skill=DSA
    @GetMapping
    public List<MatchSuggestion> suggestions(Authentication auth, @RequestParam String skill) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return matchingService.suggestPartners(user, skill);
    }
}
