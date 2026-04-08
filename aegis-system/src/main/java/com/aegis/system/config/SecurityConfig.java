package com.aegis.system.config;

import com.aegis.common.result.ResultCode;
import com.aegis.system.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)//关闭csrf
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                //未登录或token已过期
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request,
                                                   response,
                                                   authException) -> {
                            response.setStatus(ResultCode.UNAUTHORIZED.getCode());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{" +
                                    "\"code\":401," +
                                    "\"message\":\"" + ResultCode.UNAUTHORIZED.getMessage() + "\"" +
                                    "}");
                        })

                        //没有权限
                        .accessDeniedHandler(((request,
                                               response,
                                               authException) -> {
                                    response.setStatus(ResultCode.FORBIDDEN.getCode());
                                    response.setContentType("application/json;charset=UTF-8");
                                    response.getWriter().write("{" +
                                            "\"code\":403," +
                                            "\"message\":\"" + ResultCode.FORBIDDEN.getMessage() + "\"" +
                                            "}");
                                })
                        )
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}