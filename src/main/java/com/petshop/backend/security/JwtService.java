package com.petshop.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final String jwtSecret;
	private final long jwtExpiration;

	public JwtService(
			@Value("${app.jwt.secret}") String jwtSecret,
			@Value("${app.jwt.expiration}") long jwtExpiration
	) {
		if (jwtExpiration <= 0) {
			throw new IllegalArgumentException("JWT expiration must be greater than zero");
		}

		this.jwtSecret = jwtSecret;
		this.jwtExpiration = jwtExpiration;
		getSigningKey();
	}

	public String generateToken(UserDetails userDetails) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + jwtExpiration);

		return Jwts.builder()
				.subject(userDetails.getUsername())
				.issuedAt(now)
				.expiration(expiration)
				.signWith(getSigningKey())
				.compact();
	}

	public String extractUsername(String token) {
		return extractClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractClaims(token).getExpiration().before(new Date());
	}

	private Claims extractClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}
}
