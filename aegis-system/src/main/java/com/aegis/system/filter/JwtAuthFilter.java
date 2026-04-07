package com.aegis.system.filter;

import com.aegis.common.utils.JwtConstants;
import com.aegis.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            // 没有 token，直接放行
            filterChain.doFilter(request, response);
            return;
        }

        // 有 token，才去解析
        String token = header.substring(7);
        try {
            JwtConstants jwtConstants = new JwtConstants();
            Claims claims = JwtUtils.parseToken(token);
            // 存入 Security 上下文
            Long userId = Long.valueOf(claims.get(jwtConstants.getUSER_ID()).toString());
            String username = claims.get(jwtConstants.getUSERNAME()).toString();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken
                            (username, null,new ArrayList<>());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        // 放行
        filterChain.doFilter(request, response);
    }
}