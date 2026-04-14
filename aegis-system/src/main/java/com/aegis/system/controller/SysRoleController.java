package com.aegis.system.controller;

import com.aegis.common.annotation.OperationLog;
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
    @OperationLog("查询所有角色列表")
    public Result<?> list(){
        return Result.success(sysRoleService.list());
    }

    @GetMapping("/{id}")
    @OperationLog("通过ID查找角色")
    public Result<?> detail(@PathVariable Long id){
        return Result.success(sysRoleService.getById(id));
    }

    @PostMapping
    @OperationLog("保存新的角色")
    public Result<?> save(@RequestBody SysRole sysRole){
        return Result.success(sysRoleService.save(sysRole));
    }

    @PutMapping("/{id}")
    @OperationLog("更新角色")
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
    @OperationLog("删除角色")
    public Result<?> delete(@PathVariable Long id){
        return Result.success(sysRoleService.removeById(id));
    }
}
