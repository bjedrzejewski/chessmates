package com.chessmates.repository
/**
 * Repository for Game models.
 */
interface GameRepository {
    void saveAll(game)
    def find(String gameId)
    List findAll()
}