package com.chessmates.repository
/**
 * Repository for Game models.
 */
interface GameRepository {
    void save( player)
    def find(String playerId)
    List findAll()
}