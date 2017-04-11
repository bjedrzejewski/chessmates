package com.chessmates.utility

import com.chessmates.model.Game
import com.chessmates.model.Player

/**
 * An object that interacts with the Lichess API.
 */
interface LichessApi {

    LichessResultPage<Player> getPlayers(String teamId)
    LichessResultPage<Game> getGames(String playerId)

}
