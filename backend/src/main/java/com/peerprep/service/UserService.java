package com.peerprep.service;

import com.peerprep.dto.SkillDtos.AddSkillRequest;
import com.peerprep.exception.ResourceNotFoundException;
import com.peerprep.model.Skill;
import com.peerprep.model.User;
import com.peerprep.model.UserSkill;
import com.peerprep.repository.SkillRepository;
import com.peerprep.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;

    public UserService(SkillRepository skillRepository, UserSkillRepository userSkillRepository) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public UserSkill addSkill(User user, AddSkillRequest req) {
        Skill skill = skillRepository.findByNameIgnoreCase(req.skillName)
                .orElseGet(() -> skillRepository.save(new Skill(req.skillName)));

        return userSkillRepository.findByUserAndSkillAndType(user, skill, req.type)
                .map(existing -> {
                    existing.setProficiency(req.proficiency);
                    return userSkillRepository.save(existing);
                })
                .orElseGet(() -> userSkillRepository.save(
                        new UserSkill(user, skill, req.type, req.proficiency)));
    }

    public List<UserSkill> getSkills(User user) {
        return userSkillRepository.findByUser(user);
    }
}
