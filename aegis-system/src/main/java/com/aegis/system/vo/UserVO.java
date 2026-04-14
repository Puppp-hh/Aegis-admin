package com.aegis.system.vo;

import com.aegis.common.annotation.Sensitive;
import com.aegis.common.model.SensitiveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//返回给前端的查询的数据
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    private Long id;
    private String username;
    private String nickname;

    @Sensitive(type = SensitiveType.PHONE)
    private String phone;

    @Sensitive(type = SensitiveType.EMAIL)
    private String email;

    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
    private String createBy;
}
