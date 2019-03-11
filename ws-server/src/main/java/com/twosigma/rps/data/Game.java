package com.twosigma.rps.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

public class Game {
    private static final Logger logger = LoggerFactory.getLogger(Game.class);

    private final WebSocketSession[] players;
    private final RPS[] playerChoice;

    public Game(final WebSocketSession... players) {
        this.players = players;
        this.playerChoice = new RPS[2];
    }

    public WebSocketSession[] getPlayers() {
        return players;
    }

    public Outcome played(final WebSocketSession player, final RPS rps) {
        final String playerId = player.getId();
        logger.info("Player {} played {}", playerId, rps);
        return choiceOutcome(playerId, rps);
    }

    public void resetGame() {
        playerChoice[0] = playerChoice[1] = null;
    }

    private Outcome choiceOutcome(final String playerId, final RPS choice) {
        final String player1Id = players[0].getId();
        playerChoice[playerId.equals(player1Id) ? 0 : 1] = choice;
        return completedGame() ? outcome() : Outcome.UNKNOWN;
    }

    private boolean completedGame() {
        return playerChoice[0] != null && playerChoice[1] != null;
    }

    private Outcome outcome() {
        final RPS p1Choice = playerChoice[0];
        final RPS p2Choice = playerChoice[1];
        return RPS.outcome(p1Choice, p2Choice);
    }

    public static void main(String[] args) {
        for (final RPS rps1 : RPS.values()) {
            for (final RPS rps2 : RPS.values()) {
                logger.info("{} {} {}", rps1, RPS.outcome(rps1, rps2), rps2);
            }
        }
    }
}
