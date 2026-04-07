package com.aegis.app;

import com.aegis.system.entity.SysUser;
import com.aegis.system.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@SpringBootTest
public class SysUserServiceTest {

    @Autowired
    private SysUserService sysUserService;

    @Test
    public void list() {
        System.out.println(sysUserService.list());
    }

    @Test
    // 根据id查询用户
    public void getById(){
        SysUser sysUser=sysUserService.getById(1);
        System.out.println(sysUser);
    }

    @Test
    // 新增用户
    public void save() {
        SysUser sysUser = SysUser.builder()
                .username("pzy666")
                .password("102tf5ytyf")
                .nickname("管理员")
                .phone("13827861816")
                .email("3289897742@qq.com")
                .status(1)
                .createBy("systemTest")
                .build();


        sysUserService.save(sysUser);
    }

    @Test
    // 修改用户
    public void update(){
        sysUserService.update(
                null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId,2)
                        .set(SysUser::getUsername,"testName")
                        .set(SysUser::getNickname,"test管理员")
        );
    }

    @Test
    // 删除用户
    public void delete(){
        sysUserService.removeById(2);
    }

    //密码测试
    @Test
    public void testPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("admin123"));
    }
}