package com.aegis.system.service;

import com.aegis.system.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysUserService extends IService<SysUser> {
    SysUser getUserByName(String username);
}