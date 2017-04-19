package com.chessmates.service
/**
 * Service handling data requests relating to Lichess entities.
 */
interface EntityService {

    /**
     * Returns a list of team players.
     */
    List getPlayers()

    /**
     * Returns a list of games played between team players.
     */
    List getGames()

}