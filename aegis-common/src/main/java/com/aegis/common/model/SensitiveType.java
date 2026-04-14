package com.aegis.common.model;

import lombok.Getter;

@Getter
public enum SensitiveType {
    PASSWORD,
    EMAIL,
    PHONE,
    TOKEN,
    DEFAULT
}
