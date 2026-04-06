package com.aegis.system.controller;

import com.aegis.common.result.Result;
import com.aegis.system.entity.SysUser;
import com.aegis.system.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    // 查询用户列表
    @GetMapping
    public Result<?> list(){
        return Result.success(sysUserService.list());
    }

    // 根据id查询用户
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id){
        SysUser sysUser=sysUserService.getById(id);
        return Result.success(sysUser);
    }

    // 新增用户
    @PostMapping
    public Result<?> save(@RequestBody SysUser sysUser){
        sysUserService.save(sysUser);
        return Result.success(sysUser);
    }

    // 修改用户
    // 后续有需要的修改的再添加
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody SysUser sysUser){
        sysUserService.update(
                null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId,id)
                        .set(SysUser::getUsername,sysUser.getUsername())
                        .set(SysUser::getPassword,sysUser.getPassword())
                        .set(SysUser::getNickname,sysUser.getNickname())
                        .set(SysUser::getPhone,sysUser.getPhone())
                        .set(SysUser::getEmail,sysUser.getEmail())
        );
        return Result.success();
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id){
        sysUserService.removeById(id);
        return Result.success();
    }
}