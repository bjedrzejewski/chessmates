package com.chessmates.lichess.data
/**
 * An object that interacts with the Lichess API.
 */
interface LichessApi {

    /**
     * Get a page the players of a given Lichess team.
     */
    def getPlayersPage(String teamId, int pageNumber, int pageSize)

    /**
     * Get a page games of a given player.
     */
    def getGamesPage(String playerId, int pageNumber, int pageSize)

    /**
     * Get a page of games for a given player, against a given opponent.
     */
    def getGamesPage(String playerId, String opponentId, int pageNumber, int pageSize)
}
