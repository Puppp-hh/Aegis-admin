package com.aegis.system.service.impl;

import com.aegis.system.entity.SysRoleMenu;
import com.aegis.system.mapper.SysRoleMenuMapper;
import com.aegis.system.service.SysRoleMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu>
    implements SysRoleMenuService {
}
