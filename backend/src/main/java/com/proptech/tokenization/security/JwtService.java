package com.proptech.tokenization.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
	private final SecretKey signingKey;
	private final long expirationMs;

	public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expirationMs}") long expirationMs) {
		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(toBase64(secret)));
		this.expirationMs = expirationMs;
	}

	private String toBase64(String raw) {
		// Accept either a base64 string or a long raw string (dev convenience)
		if (raw == null) return "";
		if (raw.matches("^[A-Za-z0-9+/=]+$") && raw.length() % 4 == 0) return raw;
		return java.util.Base64.getEncoder().encodeToString(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(userDetails.getUsername())
				.claims(extraClaims)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(expirationMs)))
				.signWith(signingKey)
				.compact();
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> resolver) {
		return resolver.apply(extractAllClaims(token));
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username != null && username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		Date exp = extractClaim(token, Claims::getExpiration);
		return exp == null || exp.before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}

