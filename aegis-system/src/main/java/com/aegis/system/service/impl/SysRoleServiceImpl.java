package com.aegis.system.service.impl;

import com.aegis.system.entity.SysRole;
import com.aegis.system.mapper.SysRoleMapper;
import com.aegis.system.service.SysRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {
}
