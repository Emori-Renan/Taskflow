package com.taskflow.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

        @Bean
        public KeyResolver ipKeyResolver() {
                return exchange -> {
                        var remoteAddress = exchange.getRequest().getRemoteAddress();
                        String key = remoteAddress != null
                                        ? remoteAddress.getAddress().getHostAddress()
                                        : "unknown";
                        return Mono.just(key);
                };
        }

        @Bean
        @ConditionalOnBean(ReactiveRedisConnectionFactory.class)
        public RedisRateLimiter redisRateLimiter() {
                return new RedisRateLimiter(5, 10);
        }
}
