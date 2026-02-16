package com.taskflow.user.shared.security;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class JwtUserContext {

    public UUID userId() {
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String userId = attrs.getRequest().getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("X-User-Id header is missing");
        }
        return UUID.fromString(userId);
    }
}
