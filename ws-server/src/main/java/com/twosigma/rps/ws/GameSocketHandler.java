package com.twosigma.rps.ws;

import com.twosigma.rps.data.Game;
import com.twosigma.rps.data.Outcome;
import com.twosigma.rps.data.RPS;
import com.twosigma.rps.engine.GameEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static com.twosigma.rps.data.Outcome.BEATS;

@Component
public class GameSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GameSocketHandler.class);
    private final GameEngine engine;

    public GameSocketHandler(final GameEngine engine) {
        this.engine = engine;
    }

    @Override
    public void afterConnectionEstablished(final WebSocketSession player) {
        logger.info("Player {} connected", player.getId());
    }

    @Override
    protected void handleTextMessage(final WebSocketSession player, final TextMessage message) {
        final String action = message.getPayload();
        switch (action) {
            case "p|r": // player ready
                logger.info("Player {} Ready", player.getId());
                final Optional<Game> game = engine.playerJoined(player);
                game.ifPresent(g -> notifyGame(g, "g|s"));
                break;
            case "a|p":
            case "a|r":
            case "a|s":
                handleAction(player, RPS.of(action));
        }
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession player, final CloseStatus status) {
        logger.info("Player {} disconnected with {} status", player.getId(), status);
        final Optional<Game> game = engine.playerLeft(player);
        game.ifPresent(g -> Stream.of(g.getPlayers()).filter(p -> !p.getId().equals(player.getId())).forEach(p -> notifyPlayer(p, "g|e")));
    }

    private void handleAction(final WebSocketSession player, final RPS action) {
        final Outcome outcome = engine.played(player, action);
        logger.info("Game outcome {}", outcome);

        if (outcome != Outcome.UNKNOWN) {
            final Game game = engine.game(player);
            final WebSocketSession[] players = game.getPlayers();

            if (outcome == Outcome.TIE) {
                notifyGame(game, "o|t");
            } else {
                notifyPlayer(players[outcome == BEATS ? 0 : 1], "o|w");
                notifyPlayer(players[outcome == BEATS ? 1 : 0], "o|l");
            }
        }
    }

    private void notifyGame(final Game game, final String message) {
        Stream.of(game.getPlayers()).forEach(p -> notifyPlayer(p, message));
    }

    private void notifyPlayer(final WebSocketSession player, final String message) {
        try {
            if(player.isOpen()) {
                player.sendMessage(new TextMessage(message));
            }
        } catch (final IOException e) {
            logger.error("Failed to notify game");
        }
    }
}
