package com.twosigma.rps.ws;

import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Nonnull;

@Configuration
@EnableWebSocket
public class WebsocketConfiguration extends SpringBootServletInitializer implements WebSocketConfigurer {
    private final GameSocketHandler gameSocketHandler;

    public WebsocketConfiguration(final GameSocketHandler gameSocketHandler) {
        this.gameSocketHandler = gameSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(@Nonnull final WebSocketHandlerRegistry registry) {
        registry.addHandler(gameSocketHandler, "/game").setAllowedOrigins("*");
    }
}
