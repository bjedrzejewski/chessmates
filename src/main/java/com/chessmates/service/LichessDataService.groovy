package com.chessmates.service

import com.chessmates.model.Game
import com.chessmates.model.Player
import org.apache.commons.lang3.tuple.ImmutablePair

/**
 * Service for fetching data from the Lichess API.
 */
interface LichessDataService {

    /** Get all players. */
    List<Player> getPlayers()

    /** Get all players up until given id. */
    List<Player> getPlayers(String untilPlayerId)

    /** Get all games. */
    List<Game> getGames(List<Player> players)

    /** Get all games for each player until given game. */
    List<Game> getGames(List<Player> players, Map<ImmutablePair, Game> latestGameMap)

}