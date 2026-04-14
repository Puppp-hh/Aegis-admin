package com.aegis.system.service;

import com.aegis.system.entity.SysLog;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysLogService extends IService<SysLog> {
    void saveFromMQ(SysLog sysLog);
}
