package com.aegis.system.controller;

import com.aegis.common.annotation.OperationLog;
import com.aegis.common.result.Result;
import com.aegis.system.entity.SysUser;
import com.aegis.system.service.SysUserService;
import com.aegis.system.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/users")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    // 查询用户列表
    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping
    @OperationLog("列出所有用户")
    public Result<?> list(){
        ArrayList<UserVO> userVO = new ArrayList<>();
        for (SysUser sysUser : sysUserService.list()) {
            UserVO userVO1 = new UserVO();
            BeanUtils.copyProperties(sysUser, userVO1);
            userVO.add(userVO1);
        }
        return Result.success(userVO);
    }

    // 根据id查询用户
    @PreAuthorize("hasAuthority('sys:user:list')")
    @GetMapping("/{id}")
    @OperationLog("通过ID查找用户")
    public Result<?> getById(@PathVariable Long id){
        SysUser sysUser=sysUserService.getById(id);
        return Result.success(sysUser);
    }

    // 新增用户
    @PreAuthorize("hasAuthority('sys:user:add')")
    @PostMapping
    @OperationLog("新增用户")
    public Result<?> save(@RequestBody SysUser sysUser){
        sysUserService.save(sysUser);
        return Result.success(sysUser);
    }

    // 修改用户
    // 后续有需要的修改的再添加
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @PutMapping("/{id}")
    @OperationLog("更新用户信息")
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
    @PreAuthorize("hasAuthority('sys:user:delete')")
    @DeleteMapping("/{id}")
    @OperationLog("删除用户")
    public Result<?> delete(@PathVariable Long id){
        sysUserService.removeById(id);
        return Result.success();
    }
}