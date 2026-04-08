package com.aegis.system.filter;

import com.aegis.common.utils.JwtConstants;
import com.aegis.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            Claims claims = JwtUtils.parseToken(token);
            // 存入 Security 上下文
            String username = claims.get(JwtConstants.USERNAME).toString();
            List<String> permissions = (List<String>) claims.get(JwtConstants.PERMISSION);

            List<SimpleGrantedAuthority> authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken
//            第一个：username → 用户名，标识是谁
//            第二个：null → 密码，token 验证阶段不需要密码所以是 null
//            第三个 权限列表
                            (username, null,authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        // 放行
        filterChain.doFilter(request, response);
    }
}