package com.aegis.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtUtils {

    // 密钥，实际项目从配置文件读，这里先写死
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // access token 有效期 2小时
    private static final long ACCESS_TOKEN_EXPIRE = 2 * 60 * 60 * 1000L;

    // refresh token 有效期 7天
    private static final long REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60 * 1000L;

    // 生成 access token
    public static String generateAccessToken(Map<String, Object> claims) {
        return generateToken(claims, ACCESS_TOKEN_EXPIRE);
    }

    // 生成 refresh token
    public static String generateRefreshToken(Map<String, Object> claims) {
        return generateToken(claims, REFRESH_TOKEN_EXPIRE);
    }

    // 生成 token
    private static String generateToken(Map<String, Object> claims, long expire) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 解析 token，拿到里面存的数据
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 判断 token 是否过期
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // 从 token 里拿用户ID
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    // 从 token 里拿用户名
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username").toString();
    }
}