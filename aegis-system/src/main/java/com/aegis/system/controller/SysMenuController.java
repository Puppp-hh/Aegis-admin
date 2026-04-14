package com.aegis.system.controller;

import com.aegis.common.annotation.OperationLog;
import com.aegis.common.result.Result;
import com.aegis.system.entity.SysMenu;
import com.aegis.system.service.SysMenuService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/menus")
public class SysMenuController {
    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping
    @OperationLog("查询所有权限")
    public Result<?> query(){
        return Result.success(sysMenuService.list());
    }

    @GetMapping("/{id}")
    @OperationLog("通过ID查询权限")
    public Result<?> getByoId(@PathVariable Long id){
        return Result.success(sysMenuService.getById(id));
    }

    @PostMapping
    @OperationLog("设置权限")
    public Result<?> save(@RequestBody SysMenu sysMenu){
        return Result.success(sysMenuService.save(sysMenu));
    }

    @PutMapping("/{id}")
    @OperationLog("更新用户权限")
    public Result<?> update(@PathVariable Long id,@RequestBody SysMenu sysMenu){
        sysMenuService.update(
                null,
                new LambdaUpdateWrapper<SysMenu>()
                        .eq(SysMenu::getId, id)
                        .set(SysMenu::getParentId,sysMenu.getParentId())
                        .set(SysMenu::getMenuName,sysMenu.getMenuName())
                        .set(SysMenu::getMenuType,sysMenu.getMenuType())
                        .set(SysMenu::getPath,sysMenu.getPath())
                        .set(SysMenu::getComponent,sysMenu.getComponent())
                        .set(SysMenu::getPermission,sysMenu.getPermission())
                        .set(SysMenu::getIcon,sysMenu.getIcon())
                        .set(SysMenu::getSort,sysMenu.getSort())
        );
        return  Result.success();
    }

    @DeleteMapping("/{id}")
    @OperationLog("删除用户权限")
    public Result<?> delete(@PathVariable Long id){
        return Result.success(sysMenuService.removeById(id));
    }
}
