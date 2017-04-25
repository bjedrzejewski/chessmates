package com.chessmates.repository

import org.springframework.beans.factory.annotation.Autowired

/**
 * Repository that exposes all game related data operation
 * This particular implementation is utilising PostGreSql DB hosted on AWS
 */

class RdsGameRespository implements GameRepository{
    private QueryExecutor queryExecutor

    @Autowired
    RdsGameRepository(QueryExecutor queryExecutor){
        this.queryExecutor = queryExecutor
    }

    @Override
    void saveAll(Object games) {
        queryExecutor.executeInsert("games", games)
    }

    @Override
    def find(String gameId) {
        queryExecutor.executeSelect("games", gameId)
    }

    @Override
    def findAll() {
        queryExecutor.executeSelect("games")
    }
}
