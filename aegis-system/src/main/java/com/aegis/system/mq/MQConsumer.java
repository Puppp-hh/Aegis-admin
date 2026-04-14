package com.aegis.system.mq;

import com.aegis.common.exception.BusinessException;
import com.aegis.common.result.ResultCode;
import com.aegis.common.utils.JsonUtil;
import com.aegis.system.entity.SysLog;
import com.aegis.system.service.SysLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.aegis.system.config.RabbitMQConfig.QUEUE_NAME;

@Component
public class MQConsumer {

    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private SysLogService sysLogService;

    @RabbitListener(queues = QUEUE_NAME)
    public void receiveMessage(String message) {
        try {
            System.out.println("1========================");
            SysLog sysLog = jsonUtil.jsonToObject(message, SysLog.class);

            System.out.println("2========================");
            System.out.println(sysLog);
            System.out.println("3========================");
            sysLogService.saveFromMQ(sysLog);
            System.out.println("4========================");

        } catch (Exception e) {
            System.out.println("消费者报错：" + e.getMessage());
            e.printStackTrace();
            throw new BusinessException(ResultCode.MQ_CONSUMER_TACKLE.getCode(),ResultCode.MQ_CONSUMER_TACKLE.getMessage());
        }
    }
}