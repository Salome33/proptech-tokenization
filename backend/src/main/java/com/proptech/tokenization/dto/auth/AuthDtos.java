package com.proptech.tokenization.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDtos {
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RegisterRequest {
		@Email
		@NotBlank
		private String email;

		@NotBlank
		@Size(min = 6, max = 72)
		private String password;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LoginRequest {
		@Email
		@NotBlank
		private String email;

		@NotBlank
		private String password;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AuthResponse {
		private String token;
		private String email;
		private String role;
	}
}

