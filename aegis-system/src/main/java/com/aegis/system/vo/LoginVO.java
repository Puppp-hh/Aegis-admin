package com.aegis.system.vo;

import com.aegis.common.annotation.Sensitive;
import com.aegis.common.model.SensitiveType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginVO {

    @Sensitive(type = SensitiveType.TOKEN)
    private String accessToken;

    @Sensitive(type = SensitiveType.TOKEN)
    private String refreshToken;

    private String username;
    private String nickname;
    private Long userId;
    private List<String> permissions;
}
