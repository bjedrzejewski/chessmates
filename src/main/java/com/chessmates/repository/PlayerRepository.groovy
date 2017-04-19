package com.chessmates.repository
/**
 * A repository for Player models.
 */
interface PlayerRepository {

    void save(player)
    def find(String playerId)
    List findAll()

}