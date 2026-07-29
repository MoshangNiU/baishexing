package com.yunlan.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private static String secret;
    private static long expire;

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";
    private static final String CLAIM_ROLE = "role";

    @Value("${yunlan.jwt.secret}")
    public void setSecret(String secret) {
        JwtUtils.secret = secret;
    }

    @Value("${yunlan.jwt.expire}")
    public void setExpire(long expire) {
        JwtUtils.expire = expire;
    }

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long userId) {
        return buildToken(userId.toString(), ROLE_USER);
    }

    public static String generateAdminToken(Long adminId) {
        return buildToken(adminId.toString(), ROLE_ADMIN);
    }

    private static String buildToken(String subject, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expire);
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_ROLE, role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Long parseToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) return null;
        try {
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    public static String parseRole(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) return null;
        return (String) claims.get(CLAIM_ROLE);
    }

    public static boolean isAdminToken(String token) {
        return ROLE_ADMIN.equals(parseRole(token));
    }

    private static Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
}
