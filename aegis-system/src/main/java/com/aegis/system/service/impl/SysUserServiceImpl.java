package com.aegis.system.service.impl;

import com.aegis.system.entity.SysUser;
import com.aegis.system.mapper.SysUserMapper;
import com.aegis.system.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

}