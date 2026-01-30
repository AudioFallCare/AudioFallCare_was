package com.bumil.audio_fall_care.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AudioFallCare API")
                        .description("소리 기반 낙상 감지 및 보호자 알림 시스템 API")
                        .version("v1.0.0"));
    }
}
