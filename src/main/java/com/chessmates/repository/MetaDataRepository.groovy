package com.chessmates.repository

import com.google.common.collect.ImmutableMap

/**
 * A service for storing & accessing information about the current state of Lichess data.
 */
interface MetaDataRepository {

    /** Save latest player fetched from the Lichess API. */
    void saveLatestPlayer(player)

    /** Get latest player fetched from the Lichess API. */
    def getLatestPlayer()

    /** Save the latest game requested from the Lichess API for a given set of opponents. */
    void saveLatestGame(player, opponent, game)

    /** Get the latest game request from the Lichess API for a given set of opponents.
     * ImmutableMap is returned to indicate that changes to this store don't affect the store contents.
     */
    ImmutableMap getLatestGames()

}