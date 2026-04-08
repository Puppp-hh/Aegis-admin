package com.aegis.system.service.impl;

import com.aegis.system.entity.SysMenu;
import com.aegis.system.entity.SysRoleMenu;
import com.aegis.system.entity.SysUserRole;
import com.aegis.system.mapper.SysMenuMapper;
import com.aegis.system.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

//权限设置
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
    implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Override
    public List<String> getMenuIds(Long userId) {
        SysUserRole sysUserRole = sysUserRoleService.lambdaQuery()
                .eq(SysUserRole::getUserId,userId)
                .one();

        //提取sysRoleMenu中的所有id为1/2的数据
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.lambdaQuery()
                .eq(SysRoleMenu::getRoleId,sysUserRole.getRoleId())
                .list();

        //提取所有的menuId
        List<Long> menuIds = sysRoleMenuList.stream()
                .map(SysRoleMenu::getMenuId)
                .toList();

        //将在sys_menu中找到的权限提取为数组
        List<SysMenu> sysMenuList = lambdaQuery()
                .in(SysMenu::getId, menuIds)
                .list();

        return sysMenuList.stream()
                .map(SysMenu::getPermission)
                .filter(Objects::nonNull)
                .distinct()//去重
                .toList();
    }
}
