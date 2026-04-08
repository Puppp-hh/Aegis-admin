package com.aegis.system.service;

import com.aegis.system.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {
    List<String> getMenuIds(Long userId);
}
