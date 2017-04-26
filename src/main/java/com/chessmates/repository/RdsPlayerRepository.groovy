package com.chessmates.repository

import org.postgresql.PGStatement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import org.postgresql.util.PGobject

import java.sql.DriverManager;

/**
 * Repository that exposes all player related data operation
 * This particular implementation is utilising PostGreSql DB hosted on AWS
 */

@Repository
@Primary
class RdsPlayerRepository implements PlayerRepository {
    private QueryExecutor queryExecutor

    @Autowired
    RdsPlayerRepository(QueryExecutor queryExecutor){
        this.queryExecutor = queryExecutor
    }

    @Override
    void saveAll(Object players) {
        queryExecutor.executeInsert("players", players)
    }

    @Override
    def find(String playerId) {
        queryExecutor.executeSelect("players", playerId)
    }

    @Override
    List findAll() {
        queryExecutor.executeSelect("players")
    }
}
