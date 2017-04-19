package com.chessmates.lichess.data
/**
 * An object that interacts with the Lichess API.
 */
interface LichessApi {

    /**
     * Get the players of a given Lichess team.
     */
    def getPlayers(String teamId, int pageNumber, int pageSize)

    /**
     * Get games of a given player.
     */
    def getGames(String playerId, int pageNumber, int pageSize)

    /**
     * Get games for a given player, against a given opponent.
     */
    def getGames(String playerId, String opponentId, int pageNumber, int pageSize)
}
