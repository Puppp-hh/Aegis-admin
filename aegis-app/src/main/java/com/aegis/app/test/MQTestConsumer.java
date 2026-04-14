package com.aegis.app.test;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.aegis.system.config.RabbitMQConfig.QUEUE_NAME;

@Component
public class MQTestConsumer {
    @RabbitListener(queues = QUEUE_NAME)
    public void receiveMessage(String message) {
        System.out.println(message);
    }
}