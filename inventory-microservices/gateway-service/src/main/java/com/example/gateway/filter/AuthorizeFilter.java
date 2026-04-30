package com.example.gateway.filter;

import com.example.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 鉴权过滤器
 * 验证 Token 并将用户信息传递给下游服务
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtils jwtUtils;

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 白名单，不需要 Token 的路径
    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/logout",
            "/api/uploads/**",
            "/api/product/public/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 判断是否在白名单中
        for (String whitePath : WHITE_LIST) {
            if (pathMatcher.match(whitePath, path)) {
                return chain.filter(exchange);
            }
        }

        // 2. 获取 Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 3. 验证 Token
        if (token == null || !jwtUtils.validateToken(token)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 4. 解析用户信息并放入 Header 传给下游
        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);
        String role = jwtUtils.getRoleFromToken(token);

        if (userId != null) {
            ServerHttpRequest mutableRequest = request.mutate()
                    .header("X-User-Id", userId.toString())
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();
            return chain.filter(exchange.mutate().request(mutableRequest).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0; // 优先级低于 GlobalLogFilter
    }
}
