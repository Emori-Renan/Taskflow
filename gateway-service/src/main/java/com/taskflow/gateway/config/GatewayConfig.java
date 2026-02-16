package com.taskflow.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

        @Value("${gateway.services.auth-service.uri:http://auth-service:8081}")
        private String authServiceUri;

        @Value("${gateway.services.user-service.uri:http://user-service:8082}")
        private String userServiceUri;

        @Bean
        public RouteLocator routes(RouteLocatorBuilder builder,
                        RateLimiter rateLimiter,
                        KeyResolver ipKeyResolver) {
                return builder.routes()
                                .route("auth-service", r -> r.path("/api/auth/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/auth/(?<remaining>.*)",
                                                                                "/${remaining}")
                                                                .requestRateLimiter(c -> c
                                                                                .setKeyResolver(ipKeyResolver)
                                                                                .setRateLimiter(rateLimiter)))
                                                .uri(authServiceUri))

                                .route("user-service", r -> r.path("/api/users/**")
                                                .filters(f -> f
                                                                .rewritePath("/api/users/(?<remaining>.*)",
                                                                                "/${remaining}")
                                                                .requestRateLimiter(c -> c
                                                                                .setKeyResolver(ipKeyResolver)
                                                                                .setRateLimiter(rateLimiter)))
                                                .uri(userServiceUri))

                                .route("swagger-ui", r -> r.path("/swagger-ui/**")
                                                .uri(authServiceUri))
                                .route("auth-docs", r -> r.path("/v3/api-docs/**")
                                                .uri(authServiceUri))
                                .build();
        }
}
