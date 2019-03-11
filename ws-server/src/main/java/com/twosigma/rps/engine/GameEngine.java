package com.twosigma.rps.engine;

import com.google.common.collect.Maps;
import com.twosigma.rps.data.Game;
import com.twosigma.rps.data.Outcome;
import com.twosigma.rps.data.RPS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;

import static com.twosigma.rps.data.Outcome.UNKNOWN;

@Component
public class GameEngine {
    private static final Logger logger = LoggerFactory.getLogger(GameEngine.class);
    private final Map<String, WebSocketSession> waitingPlayers;
    private final Map<String, Game> ongoingGames;

    public GameEngine() {
        this.waitingPlayers = Maps.newLinkedHashMap();
        this.ongoingGames = Maps.newConcurrentMap();
    }

    public Optional<Game> playerJoined(final WebSocketSession player) {
        logger.info("Player {} joined", player.getId());
        final boolean hasWaiting = waitingPlayers.isEmpty();
        final Game game;
        if (hasWaiting) {
            waitingPlayers.put(player.getId(), player);
            game = null;
        } else {
            final WebSocketSession player1 = waitingPlayers.values().iterator().next();
            waitingPlayers.remove(player1.getId());

            game = new Game(player1, player);
            ongoingGames.put(player1.getId(), game);
            ongoingGames.put(player.getId(), game);
        }

        return Optional.ofNullable(game);
    }

    public Optional<Game> playerLeft(final WebSocketSession player) {
        logger.info("Player {} left", player.getId());
        waitingPlayers.remove(player.getId());
        return Optional.ofNullable(ongoingGames.remove(player.getId()));
    }

    public Outcome played(final WebSocketSession player, final RPS rps) {
        logger.info("Player {} played {}", player.getId(), rps);
        final Optional<Game> game = Optional.ofNullable(ongoingGames.get(player.getId()));
        return game.map(g -> gameOutcome(g, player, rps)).orElse(UNKNOWN);
    }

    private Outcome gameOutcome(final Game game, final WebSocketSession player, final RPS action) {
        final Outcome outcome = game.played(player, action);
        if (outcome != UNKNOWN) {
            game.resetGame();
        }
        return outcome;
    }

    public Game game(final WebSocketSession player) {
        return ongoingGames.get(player.getId());
    }
}
