package com.loopers.infrastructure.pg;

import feign.Logger;
import org.springframework.context.annotation.Bean;

public class PgFeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
