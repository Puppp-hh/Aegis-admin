package com.aegis.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginVO {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String nickname;
    private Long userId;
    private List<String> permissions;
}
