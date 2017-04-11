package com.chessmates.utility

import com.chessmates.model.Game
import com.chessmates.model.Player

/**
 * An object that interacts with the Lichess API.
 */
interface LichessApi {

    /**
     * Get the players of a given Lichess team.
     */
    LichessResultPage<Player> getPlayers(String teamId, int pageNumber)
    /**
     * Get games of a given player.
     */
    LichessResultPage<Game> getGames(String playerId, int pageNumber)

}
