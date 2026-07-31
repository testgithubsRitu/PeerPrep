package com.peerprep.repository;

import com.peerprep.model.Skill;
import com.peerprep.model.User;
import com.peerprep.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUser(User user);

    // All OFFER-type rows for a given skill, excluding the current user - these are match candidates
    List<UserSkill> findBySkillAndTypeAndUserNot(Skill skill, UserSkill.Type type, User user);

    Optional<UserSkill> findByUserAndSkillAndType(User user, Skill skill, UserSkill.Type type);
}
