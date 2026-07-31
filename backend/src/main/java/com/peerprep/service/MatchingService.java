package com.peerprep.service;

import com.peerprep.dto.SkillDtos.MatchSuggestion;
import com.peerprep.model.*;
import com.peerprep.repository.SkillRepository;
import com.peerprep.repository.UserSkillRepository;
import com.peerprep.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Ranks candidate practice partners for a given user + skill.
 *
 * The score is a weighted blend of four signals, each normalized to 0-1:
 *   1. Proficiency fit (40%)   - rewards a candidate who is roughly one level
 *                                above the requester (a realistic "mentor gap"),
 *                                without over-penalizing equal-level peers.
 *   2. Rating (25%)            - candidate's average post-session rating.
 *                                New users get a neutral default so they aren't
 *                                buried by the classic cold-start problem.
 *   3. Availability overlap (20%) - Jaccard similarity between preferred slot sets.
 *   4. Experience (15%)        - sessions completed, capped so a handful of
 *                                power users can't dominate every match list.
 *
 * Weights are constants below so they're easy to tune/justify in an interview.
 */
@Service
public class MatchingService {

    private static final double WEIGHT_PROFICIENCY = 0.40;
    private static final double WEIGHT_RATING = 0.25;
    private static final double WEIGHT_AVAILABILITY = 0.20;
    private static final double WEIGHT_EXPERIENCE = 0.15;
    private static final double NEUTRAL_RATING_FOR_NEW_USERS = 3.5; // out of 5
    private static final int EXPERIENCE_CAP_SESSIONS = 10;

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;

    public MatchingService(SkillRepository skillRepository, UserSkillRepository userSkillRepository) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public List<MatchSuggestion> suggestPartners(User requester, String skillName) {
        Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown skill: " + skillName));

        UserSkill.Proficiency requesterLevel = userSkillRepository
                .findByUserAndSkillAndType(requester, skill, UserSkill.Type.WANT)
                .map(UserSkill::getProficiency)
                .orElse(UserSkill.Proficiency.BEGINNER);

        List<UserSkill> candidates = userSkillRepository
                .findBySkillAndTypeAndUserNot(skill, UserSkill.Type.OFFER, requester);

        Set<String> requesterSlots = parseSlots(requester.getPreferredSlots());

        return candidates.stream()
                .map(candidateSkill -> {
                    User candidate = candidateSkill.getUser();
                    double proficiencyScore = proficiencyFit(requesterLevel, candidateSkill.getProficiency());
                    double ratingScore = ratingScore(candidate);
                    double availabilityScore = jaccardOverlap(requesterSlots, parseSlots(candidate.getPreferredSlots()));
                    double experienceScore = Math.min(candidate.getSessionsCompleted(), EXPERIENCE_CAP_SESSIONS)
                            / (double) EXPERIENCE_CAP_SESSIONS;

                    double total = (WEIGHT_PROFICIENCY * proficiencyScore)
                            + (WEIGHT_RATING * ratingScore)
                            + (WEIGHT_AVAILABILITY * availabilityScore)
                            + (WEIGHT_EXPERIENCE * experienceScore);

                    return new MatchSuggestion(
                            candidate.getId(),
                            candidate.getName(),
                            candidate.getCollege(),
                            round(candidate.getAverageRating()),
                            candidate.getSessionsCompleted(),
                            candidateSkill.getProficiency().name(),
                            round(total * 100)
                    );
                })
                .sorted(Comparator.comparingDouble((MatchSuggestion m) -> m.matchScore).reversed())
                .collect(Collectors.toList());
    }

    /** Rewards a candidate ~1 proficiency level above the requester (a realistic mentor gap). */
    private double proficiencyFit(UserSkill.Proficiency requesterLevel, UserSkill.Proficiency candidateLevel) {
        int req = requesterLevel.ordinal();
        int cand = candidateLevel.ordinal();
        int idealGap = 1;
        int actualGap = cand - req;
        double distanceFromIdeal = Math.abs(actualGap - idealGap);
        // Max possible distance is 3 (e.g. requester ADVANCED wanting a BEGINNER "mentor" gap of -2 vs ideal +1)
        return Math.max(0, 1 - (distanceFromIdeal / 3.0));
    }

    private double ratingScore(User candidate) {
        double rating = candidate.getRatingCount() == 0 ? NEUTRAL_RATING_FOR_NEW_USERS : candidate.getAverageRating();
        return rating / 5.0;
    }

    private double jaccardOverlap(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 1.0; // no stated preference = assume flexible, don't penalize
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return union.isEmpty() ? 1.0 : (double) intersection.size() / union.size();
    }

    private Set<String> parseSlots(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private double round(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
