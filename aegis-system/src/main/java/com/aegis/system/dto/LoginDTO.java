package com.aegis.system.dto;

import com.aegis.common.annotation.Sensitive;
import com.aegis.common.model.SensitiveType;
import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    @Sensitive(type = SensitiveType.PASSWORD)
    private String password;
}
