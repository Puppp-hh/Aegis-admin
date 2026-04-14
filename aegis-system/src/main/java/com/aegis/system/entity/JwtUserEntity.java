package com.aegis.system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtUserEntity {
    private Integer userId;
    private String username;
}
