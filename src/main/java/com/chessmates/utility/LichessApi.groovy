package com.chessmates.utility

import com.chessmates.model.Game
import com.chessmates.model.Player

/**
 * An object that interacts with the Lichess API.
 */
interface LichessApi {

    List<Player> getPlayers(String teamId)
    List<Game> getGames(String playerId)

}
