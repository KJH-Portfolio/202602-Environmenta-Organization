package com.kh.spring.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // http://localhost:8080/spring/swagger-ui/index.html
    @Bean
    public OpenAPI openAPI() {

        // 1. 보안 요구사항(SecurityRequirement) 정의
        // 모든 API 호출 시 "JWT_Auth"라는 이름의 보안 설정을 사용하겠다고 명시합니다.
        String securityJwtName = "JWT_Auth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityJwtName);

        // 2. 보안 스키마(SecurityScheme) 설정
        // 실제 어떤 방식으로 인증할지 정의합니다. (Bearer 방식의 JWT)
        Components components = new Components()
                .addSecuritySchemes(securityJwtName, new SecurityScheme()
                        .name(securityJwtName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .bearerFormat("JWT")); // 명세서에 JWT임을 명시

        return new OpenAPI()
                .info(new Info()
                        .title("EasyEarth Project API")
                        .description("EasyEarth Project API 문서 (JWT 인증 포함)")
                        .version("v1.0.0"))
                .addSecurityItem(securityRequirement) // 전역 보안 설정 적용
                .components(components);
    }
}