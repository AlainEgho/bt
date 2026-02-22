package com.example.backend.config;

import com.example.backend.security.JwtTokenProvider;
import com.example.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final com.example.backend.repository.UserRepository userRepository;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(
                            org.springframework.http.server.ServerHttpRequest request,
                            org.springframework.web.socket.WebSocketHandler handler,
                            Map<String, Object> attributes) {
                        String query = request.getURI().getQuery();
                        String token = null;
                        if (query != null) {
                            for (String param : query.split("&")) {
                                if (param.startsWith("token=")) {
                                    token = param.substring(6);
                                    break;
                                }
                            }
                        }
                        if (token == null || token.isBlank()) {
                            return null;
                        }
                        if (!jwtTokenProvider.validateToken(token)) {
                            return null;
                        }
                        Long userId = jwtTokenProvider.getUserIdFromToken(token);
                        return userRepository.findById(userId)
                                .map(user -> (Principal) new UsernamePasswordAuthenticationToken(
                                        UserPrincipal.create(user),
                                        null,
                                        UserPrincipal.create(user).getAuthorities()))
                                .orElse(null);
                    }
                })
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
