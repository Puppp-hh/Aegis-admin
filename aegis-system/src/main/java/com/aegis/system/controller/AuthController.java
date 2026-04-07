package com.aegis.system.controller;

import com.aegis.common.result.Result;
import com.aegis.common.utils.JwtConstants;
import com.aegis.common.utils.JwtUtils;
import com.aegis.system.dto.LoginDTO;
import com.aegis.system.entity.SysUser;
import com.aegis.system.service.SysUserService;
import com.aegis.system.vo.LoginVO;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO){
        SysUser sysUser = sysUserService.getUserByName(loginDTO.getUsername());

        if (sysUser == null){
            throw new UsernameNotFoundException("未找到用户名:"+loginDTO.getUsername());
        }

        if(!passwordEncoder.matches(loginDTO.getPassword(),sysUser.getPassword())){
            throw new UsernameNotFoundException("密码错误!");
        }

        JwtConstants jwtConstants = new JwtConstants();
        Map<String ,Object> map = new HashMap<>();
        map.put(jwtConstants.getUSERNAME(), sysUser.getUsername());
        map.put(jwtConstants.getUSER_ID(), sysUser.getId());

        String accessToken=JwtUtils.generateAccessToken(map);
        String refreshToken=JwtUtils.generateRefreshToken(map);

        LoginVO loginVo=new LoginVO(accessToken,refreshToken,sysUser.getUsername(),sysUser.getNickname(),sysUser.getId());

        return  Result.success(loginVo);
    }
}
