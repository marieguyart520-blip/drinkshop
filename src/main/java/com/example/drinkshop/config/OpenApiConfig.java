package com.example.drinkshop.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI posOpenApi() {
        return new OpenAPI()
            .info(new Info().title("手搖飲 POS API").version("v1")
                .description("使用 Authorize 輸入有效員工帳密，再以 Try it out 測試 API。寫入操作會修改目前連線的資料庫。"))
            .components(new Components().addSecuritySchemes("basicAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")
                    .description("有效的員工帳號與密碼（HTTP Basic）")))
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
