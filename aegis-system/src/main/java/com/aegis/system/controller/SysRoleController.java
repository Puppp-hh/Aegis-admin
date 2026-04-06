package com.aegis.system.controller;

import com.aegis.common.result.Result;
import com.aegis.system.entity.SysRole;
import com.aegis.system.service.SysRoleService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
public class SysRoleController {
    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping
    public Result<?> list(){
        return Result.success(sysRoleService.list());
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id){
        return Result.success(sysRoleService.getById(id));
    }

    @PostMapping
    public Result<?> save(@RequestBody SysRole sysRole){
        return Result.success(sysRoleService.save(sysRole));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody SysRole sysRole){
        sysRoleService.update(
                null,
                new LambdaUpdateWrapper<SysRole>()
                        .eq(SysRole::getId,id)
                        .set(SysRole::getRoleName,sysRole.getRoleName())
                        .set(SysRole::getDescription,sysRole.getDescription())
        );
        return Result.success();
    }

    @DeleteMapping("{id}")
    public Result<?> delete(@PathVariable Long id){
        return Result.success(sysRoleService.removeById(id));
    }
}
