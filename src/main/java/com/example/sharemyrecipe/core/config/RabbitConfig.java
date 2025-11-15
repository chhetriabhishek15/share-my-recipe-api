package com.example.sharemyrecipe.core.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String IMAGE_RESIZE_QUEUE = "image.resize.queue";

    @Bean
    public Queue imageResizeQueue() {
        // durable queue
        return new Queue(IMAGE_RESIZE_QUEUE, true);
    }
}
