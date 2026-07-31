package com.peerprep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public static class RegisterRequest {
        @NotBlank
        public String name;
        @Email @NotBlank
        public String email;
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
        public String password;
        public String college;
        public String preferredSlots; // e.g. "WEEKDAY_EVENING,WEEKEND_MORNING"
    }

    public static class LoginRequest {
        @NotBlank
        public String email;
        @NotBlank
        public String password;
    }

    public static class AuthResponse {
        public String token;
        public Long userId;
        public String name;

        public AuthResponse(String token, Long userId, String name) {
            this.token = token;
            this.userId = userId;
            this.name = name;
        }
    }
}
