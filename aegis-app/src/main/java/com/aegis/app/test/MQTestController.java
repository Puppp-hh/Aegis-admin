package com.aegis.app.test;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.aegis.system.config.RabbitMQConfig.EXCHANGE_NAME;

@RestController
@RequestMapping("/test/pzy")
public class MQTestController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/send/{message}")
    public String send(@PathVariable String message) {
        rabbitTemplate.convertAndSend(
                EXCHANGE_NAME,
                "boot.test_RabbitMQ",
                message);

        return "success";
    }
}
