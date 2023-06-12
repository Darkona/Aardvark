package com.darkona.aardvark.security;

import com.darkona.aardvark.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JWTGeneratorImpl implements JwtGenerator {

    @Value("${aardvark.jwt.secret}")
    private String secret;

    @Value("${aardvark.jwt.message}")
    private String message;
    @Override
    public String generateToken(User user) {
        return Jwts.builder()
                .claim("Message", message + " " + UUID.randomUUID())
                .setSubject(user.getEmail())
                .setIssuer("Aardvark")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(60 * 15)))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}
