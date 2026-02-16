package com.taskflow.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "jwt.secret=dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLWhtYWMtc2hhMjU2",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
        "spring.cloud.gateway.server.webflux.default-filters[0].name=PreserveHostHeader"
})
@AutoConfigureWebTestClient
@Import(GatewayRoutingIntegrationTest.NoOpRateLimiterConfig.class)
class GatewayRoutingIntegrationTest {

    private static final WireMockServer wireMock;

    static {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        wireMock.stubFor(post("/register")
                .willReturn(okJson("{\"message\":\"registered\"}")));

        wireMock.stubFor(post("/login")
                .willReturn(okJson("{\"token\":\"mock-jwt\"}")));
    }

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private ReactiveRedisConnectionFactory redisConnectionFactory;

    @MockitoBean
    private ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    @TestConfiguration
    static class NoOpRateLimiterConfig {

        @Bean
        @Primary
        public RateLimiter<Object> noOpRateLimiter() {
            return new RateLimiter<>() {
                private final Map<String, Object> configMap = new HashMap<>();

                @Override
                public Mono<Response> isAllowed(String routeId, String id) {
                    return Mono.just(new Response(true, Map.of()));
                }

                @Override
                public Class<Object> getConfigClass() {
                    return Object.class;
                }

                @Override
                public Object newConfig() {
                    return new Object();
                }

                @Override
                public Map<String, Object> getConfig() {
                    return configMap;
                }
            };
        }

        @Bean
        @Primary
        public KeyResolver noOpKeyResolver() {
            return exchange -> Mono.just("test");
        }
    }

    @BeforeEach
    void resetWireMock() {
        wireMock.resetRequests();
    }

    @AfterAll
    static void shutdown() {
        wireMock.stop();
    }

    @DynamicPropertySource
    static void gatewayProperties(DynamicPropertyRegistry registry) {
        registry.add("gateway.services.auth-service.uri",
                () -> "http://localhost:" + wireMock.port());
    }

    // ---------------------------
    // PUBLIC ROUTES
    // ---------------------------

    @Test
    @DisplayName("POST /api/auth/register is public")
    void register_isPublic() {
        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue("{\"email\":\"test@test.com\",\"password\":\"123\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("registered");

        wireMock.verify(postRequestedFor(urlEqualTo("/register")));
    }

    @Test
    @DisplayName("POST /api/auth/login is public")
    void login_isPublic() {
        webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue("{\"email\":\"test@test.com\",\"password\":\"123\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").exists();

        wireMock.verify(postRequestedFor(urlEqualTo("/login")));
    }

    // ---------------------------
    // FALLBACK
    // ---------------------------

    @Test
    @DisplayName("Unknown route returns 404")
    void unknownRoute_returns404() {
        webTestClient.get()
                .uri("/api/unknown/resource")
                .exchange()
                .expectStatus().isNotFound();
    }
}
