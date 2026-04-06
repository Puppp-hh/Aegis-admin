package com.aegis.system.controller;

import com.aegis.common.result.Result;
import com.aegis.system.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logs")
public class SysLogController {
    @Autowired
    private SysLogService sysLogService;

    @GetMapping
    public Result<?> get(){
        return Result.success(sysLogService.list());
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id){
        return Result.success(sysLogService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id){
        return Result.success(sysLogService.removeById(id));
    }
}
