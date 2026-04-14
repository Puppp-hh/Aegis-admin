package com.aegis.system.entity;

import com.aegis.common.annotation.Sensitive;
import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.aegis.common.model.SensitiveType.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @Sensitive(type = PASSWORD)
    private String password;

    @Sensitive(type = PHONE)
    private String phone;

    @Sensitive(type = EMAIL)
    private String email;

    private String nickname;
    private String avatar;
    private Integer status;
    private String createBy;
    private String updateBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}