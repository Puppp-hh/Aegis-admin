package com.aegis.system.service.impl;

import com.aegis.system.entity.SysLog;
import com.aegis.system.mapper.SysLogMapper;
import com.aegis.system.service.SysLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog>
    implements SysLogService {

    @Override
    public void saveFromMQ(SysLog sysLog){
        this.save(sysLog);
    }

}