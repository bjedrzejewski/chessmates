package com.chessmates.model

/**
 * A Lichess game.
 */
class Game {
    // TODO: Store more fields?
    String id
    Map<GameColor, String> players

    String getId() {
        return id
    }

    Map<GameColor, String> getPlayers() {
        return players
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Game game = (Game) o

        if (id != game.id) return false

        return true
    }

    int hashCode() {
        return id.hashCode()
    }
}
