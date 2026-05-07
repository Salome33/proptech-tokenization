package com.proptech.tokenization.auth;

import com.proptech.tokenization.dto.auth.AuthDtos;
import com.proptech.tokenization.model.User;
import com.proptech.tokenization.repository.UserRepository;
import com.proptech.tokenization.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
		String email = req.getEmail().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("El email ya está registrado.");
		}
		User user = userRepository.save(User.builder()
				.email(email)
				.password(passwordEncoder.encode(req.getPassword()))
				.role("ROLE_USER")
				.build());

		String token = jwtService.generateToken(
				org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
						.password(user.getPassword())
						.roles("USER")
						.build(),
				Map.of("role", "USER")
		);

		return AuthDtos.AuthResponse.builder()
				.token(token)
				.email(user.getEmail())
				.role("USER")
				.build();
	}

	public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
		Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(req.getEmail().trim().toLowerCase(), req.getPassword())
		);
		String email = auth.getName();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas."));

		String role = user.getRole().replace("ROLE_", "");
		String token = jwtService.generateToken(
				org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
						.password(user.getPassword())
						.roles(role)
						.build(),
				Map.of("role", role)
		);

		return AuthDtos.AuthResponse.builder()
				.token(token)
				.email(user.getEmail())
				.role(role)
				.build();
	}
}

