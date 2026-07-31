package com.peerprep.dto;

import com.peerprep.model.UserSkill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SkillDtos {

    public static class AddSkillRequest {
        @NotBlank
        public String skillName;
        @NotNull
        public UserSkill.Type type; // OFFER or WANT
        @NotNull
        public UserSkill.Proficiency proficiency;
    }

    public static class MatchSuggestion {
        public Long userId;
        public String name;
        public String college;
        public double averageRating;
        public int sessionsCompleted;
        public String proficiency;
        public double matchScore; // 0-100, see MatchingService for the breakdown

        public MatchSuggestion(Long userId, String name, String college, double averageRating,
                                int sessionsCompleted, String proficiency, double matchScore) {
            this.userId = userId;
            this.name = name;
            this.college = college;
            this.averageRating = averageRating;
            this.sessionsCompleted = sessionsCompleted;
            this.proficiency = proficiency;
            this.matchScore = matchScore;
        }
    }
}
