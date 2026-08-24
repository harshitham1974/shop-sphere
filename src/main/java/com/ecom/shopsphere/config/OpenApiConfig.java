package com.ecom.shopsphere.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "ShopSphere API",
                version = "1.0",
                description = "REST API for ShopSphere E-Commerce Application"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Bean
    public OpenAPI shopSphereOpenAPI() {

        return new OpenAPI()

                .info(

                        new Info()

                                .title("ShopSphere REST API")

                                .version("1.0")

                                .description("REST APIs for the ShopSphere E-Commerce Backend.")

                                .contact(

                                        new Contact()

                                                .name("Harshitha M")

                                                .url("https://github.com/harshitham1974"))

                                .license(

                                        new License()

                                                .name("MIT License")));
    }
}