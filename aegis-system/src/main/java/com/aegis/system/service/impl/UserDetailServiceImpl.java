package com.aegis.system.service.impl;

import com.aegis.system.entity.SysUser;
import com.aegis.system.service.SysUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getUserByName(username);

        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在");

        }

        if (sysUser.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被封禁");

        }
        return User.builder()
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .roles("USER")
                .build();

    }
}
