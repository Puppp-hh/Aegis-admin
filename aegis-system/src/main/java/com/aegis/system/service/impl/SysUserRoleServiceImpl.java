package com.aegis.system.service.impl;

import com.aegis.system.entity.SysUserRole;
import com.aegis.system.mapper.SysUserRoleMapper;
import com.aegis.system.service.SysUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
        implements SysUserRoleService {
}
