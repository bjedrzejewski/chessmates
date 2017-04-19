package com.chessmates.lichess.data
/**
 * Service for fetching data from the Lichess API.
 */
interface LichessDataService {

    /** Get all players. */
    List getPlayers()

    /** Get all games. */
    List getGames(List players)


}