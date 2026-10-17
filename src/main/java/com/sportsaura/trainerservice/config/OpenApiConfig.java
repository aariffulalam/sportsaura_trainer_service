package com.sportsaura.trainerservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Swagger UI setup, including the "Authorize" button for pasting a JWT from the user service. */
@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "SportsAura Trainer Service API",
            version = "v1",
            description = "Sports catalog, trainer profiles and website page content."))
@SecurityScheme(
    name = OpenApiConfig.BEARER_SCHEME,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT")
public class OpenApiConfig {

  public static final String BEARER_SCHEME = "bearerAuth";
}
