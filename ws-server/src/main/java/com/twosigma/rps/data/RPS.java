package com.twosigma.rps.data;

public enum RPS {
    ROCK,
    PAPER,
    SCISSOR;

    public static RPS of(final String action) {
        switch (action) {
            case "a|r":
                return ROCK;
            case "a|p":
                return PAPER;
            default:
                return SCISSOR;
        }
    }

    public static Outcome outcome(final RPS player, final RPS opponent) {
        if (player == opponent) return Outcome.TIE;
        switch (player) {
            case ROCK:
                return opponent == SCISSOR ? Outcome.BEATS : Outcome.LOSES;
            case PAPER:
                return opponent == ROCK ? Outcome.BEATS : Outcome.LOSES;
            case SCISSOR:
                return opponent == PAPER ? Outcome.BEATS : Outcome.LOSES;
        }
        return Outcome.UNKNOWN;
    }
}
