package com.chessmates.service

import com.chessmates.model.Game
import com.chessmates.model.Player

/**
 * Service handling data requests relating to Lichess entities.
 */
interface EntityService {

    /**
     * Returns a list of team players.
     */
    List<Player> getPlayers()

    /**
     * Returns a list of games played between team players.
     */
    List<Game> getAllTeamGames()

    /**
     * Get new games between scott logic players up to the latest game we have already fetched.
     */
    List<Game> getGamesUntilLatestFetched()


}