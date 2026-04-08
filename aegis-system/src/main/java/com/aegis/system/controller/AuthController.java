package com.aegis.system.controller;

import com.aegis.common.result.Result;
import com.aegis.common.utils.JwtConstants;
import com.aegis.common.utils.JwtUtils;
import com.aegis.system.dto.LoginDTO;
import com.aegis.system.entity.SysUser;
import com.aegis.system.service.SysUserService;
import com.aegis.system.vo.LoginVO;
import com.aegis.system.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SysMenuService sysMenuService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO){
        SysUser sysUser = sysUserService.getUserByName(loginDTO.getUsername());

        if (sysUser == null){
            throw new UsernameNotFoundException("未找到用户名:"+loginDTO.getUsername());
        }

        if(!passwordEncoder.matches(loginDTO.getPassword(),sysUser.getPassword())){
            throw new UsernameNotFoundException("密码错误!");
        }

        Map<String ,Object> map = new HashMap<>();

        //权限表
        List<String> sysMenuRoleList = sysMenuService.getMenuIds(sysUser.getId());

        //存入用户名
        map.put(JwtConstants.USERNAME, sysUser.getUsername());
        //存入用户ID
        map.put(JwtConstants.USER_ID, sysUser.getId());
        //存入权限
        map.put(JwtConstants.PERMISSION,sysMenuRoleList);

        String accessToken=JwtUtils.generateAccessToken(map);
        String refreshToken=JwtUtils.generateRefreshToken(map);

        //注入loginVo
        LoginVO loginVo=new LoginVO(accessToken,refreshToken,sysUser.getUsername(),sysUser.getNickname(),sysUser.getId(),sysMenuRoleList);

        return Result.success(loginVo);
    }
}
