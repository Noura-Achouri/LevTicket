package com.leviathan.bidpass.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    /**
     * Token validity: 7 days.
     */
    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 24 * 7;

    /**
     * Stable signing secret, configurable via "app.jwt.secret".
     * IMPORTANT: a fixed default keeps tokens valid across server restarts.
     * (Previously the secret was randomly generated on every boot, which
     * invalidated all existing sessions whenever the app restarted.)
     */
    @Value("${app.jwt.secret:LevTicket-2026-SuperSecret-Key-Must-Be-Long-Enough-For-HS256}")
    private String secret;

    /**
     * HS256 requires a key of at least 256 bits (32 bytes). If the configured
     * secret is too short, pad it; if it's long enough, use it as-is.
     */
    private SecretKey signingKey() {
        byte[] raw = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (raw.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(raw, 0, padded, 0, raw.length);
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(raw);
    }

    public String generateToken(Integer userId, String username) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Integer getUserIdFromToken(String token) {
        return Integer.valueOf(parseToken(token).getSubject());
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
