package com.fuel.nexus.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI fuelAgencyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gas & Liquid Fuel Agency Management System API")
                        .description("API documentation for managing Products and Fuel Inventory")
                        .version("1.0.0"));
    }
}

