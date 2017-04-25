package com.chessmates.repository
/**
 * A repository for Player models.
 */
interface PlayerRepository {
    void saveAll(players)
    def find(playerId)
    def findAll()
}