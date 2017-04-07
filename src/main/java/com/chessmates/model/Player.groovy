package com.chessmates.model

/**
 * A Lichess player
 */
class Player {
    // TODO: Store more fields?
    /**
     * Lichess ID for the player entity.
     */
    String id

    /**
     * Lichess username. The Lichess API provides both an ID and a username for a given player. At time of writing, these
     * are always equal.
     */
    String username


    String getId() { id }

    String getUsername() { username }

}
