package com.peerprep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class SessionDtos {

    public static class CreateSessionRequest {
        @NotNull
        public Long partnerId;
        @NotBlank
        public String skillName;
        @NotNull
        public LocalDateTime scheduledTime;
    }

    public static class FeedbackRequest {
        @NotNull
        public Integer rating; // 1-5
        public String comment;
    }
}
