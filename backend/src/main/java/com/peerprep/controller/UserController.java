package com.peerprep.controller;

import com.peerprep.dto.SkillDtos.AddSkillRequest;
import com.peerprep.model.User;
import com.peerprep.model.UserSkill;
import com.peerprep.repository.UserRepository;
import com.peerprep.exception.ResourceNotFoundException;
import com.peerprep.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public User me(Authentication auth) {
        return currentUser(auth);
    }

    @PostMapping("/me/skills")
    public UserSkill addSkill(Authentication auth, @Valid @RequestBody AddSkillRequest req) {
        return userService.addSkill(currentUser(auth), req);
    }

    @GetMapping("/me/skills")
    public List<UserSkill> mySkills(Authentication auth) {
        return userService.getSkills(currentUser(auth));
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
