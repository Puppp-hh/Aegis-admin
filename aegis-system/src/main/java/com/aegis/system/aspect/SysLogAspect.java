package com.aegis.system.aspect;

import com.aegis.common.exception.BusinessException;
import com.aegis.common.result.Result;
import com.aegis.common.utils.JsonUtil;
import com.aegis.system.entity.JwtUserEntity;
import com.aegis.system.entity.SysLog;
import com.aegis.system.filter.LogFilter;
import com.aegis.system.vo.LoginVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.aegis.common.annotation.OperationLog;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

import static com.aegis.system.config.RabbitMQConfig.EXCHANGE_NAME;

@Aspect
@Component
public class SysLogAspect {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private LogFilter LogFilter;
    @Autowired
    private JsonUtil jsonUtil;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog)
            throws Throwable {

        //方法开始前记录
        Long startTime = System.currentTimeMillis();
        LocalDateTime localDateTime = LocalDateTime.now();

        RequestAttributes judge = RequestContextHolder.getRequestAttributes();

        if (judge == null) {
            throw new IllegalStateException("无法获取请求信息");
        }
        //记录path ip
        ServletRequestAttributes request =
                (ServletRequestAttributes) judge;

        String path = request.getRequest().getRequestURI();
        String ip = request.getRequest().getRemoteAddr();

        // 执行目标方法
        int status;
        Object result;
        try {
            result = joinPoint.proceed();
            status = 1;
        } catch (Exception e) {
            status = 0;
            throw new BusinessException(status, String.valueOf(e));
        }

        //求运行时间
        Long endTime = System.currentTimeMillis();
        Long costTime = endTime - startTime;

        List<Object> filteredParams = LogFilter.logFilter(joinPoint, result);

        Long userId;
        String username;
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (principal instanceof JwtUserEntity userEntity) {
            // 已登录的请求
            userId = Long.valueOf(userEntity.getUserId());
            username = userEntity.getUsername();
        } else if (result instanceof Result<?> res && res.getData() instanceof LoginVO loginVO) {
            // 登录接口
            userId = loginVO.getUserId();
            username = loginVO.getUsername();
        } else {
            // 既没有 token 也不是登录接口，记录为匿名
            userId = null;
            username = "anonymous";
        }

        SysLog sysLog = SysLog.builder()
                .userId(userId)
                .operation(operationLog.value())
                .method(joinPoint.getSignature().getName())
                .path(path)
                .params(jsonUtil.objectToJson(filteredParams))
                .result(String.valueOf(result))
                .ip(ip)
                .operator(username)
                .timeCost(costTime)
                .status(status)
                .createBy("SysLogAspect")
                .updateBy("SysLogAspect")
                .createTime(localDateTime)
                .updateTime(localDateTime)
                .build();

        //发送日志到RabbitMQ
        rabbitTemplate.convertAndSend(
                EXCHANGE_NAME,
                "boot.SysLogAspect",
                jsonUtil.objectToJson(sysLog)
        );

        return result;
    }
}
