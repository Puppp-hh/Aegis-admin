package com.aegis.system.controller;

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
    public Result<?> query(){
        return Result.success(sysMenuService.list());
    }

    @GetMapping("/{id}")
    public Result<?> getByoId(@PathVariable Long id){
        return Result.success(sysMenuService.getById(id));
    }

    @PostMapping
    public Result<?> save(@RequestBody SysMenu sysMenu){
        return Result.success(sysMenuService.save(sysMenu));
    }

    @PutMapping("/{id}")
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
    public Result<?> delete(@PathVariable Long id){
        return Result.success(sysMenuService.removeById(id));
    }
}
