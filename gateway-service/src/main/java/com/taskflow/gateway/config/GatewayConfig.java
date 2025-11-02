package com.taskflow.gateway.config;

import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter; // Required for type hint
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress());
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(5, 10);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder,
                               RedisRateLimiter redisRateLimiter,
                               RequestRateLimiterGatewayFilterFactory rateLimiterFactory, 
                               KeyResolver ipKeyResolver) { 
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f
                                .requestRateLimiter(c -> c
                                        .setKeyResolver(ipKeyResolver)
                                        .setRateLimiter(redisRateLimiter) 
                                )
                        )
                        .uri("http://auth-service:8081"))

                // .route("user-service", r -> r.path("/api/users/**")
                //         .filters(f -> f
                //                 .requestRateLimiter(c -> c
                //                         .setKeyResolver(ipKeyResolver)
                //                         .setRateLimiter(redisRateLimiter) 
                //                 )
                //         )
                //         .uri("http://user-service:8082"))
                .build();
    }
}
