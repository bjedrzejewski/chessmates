package com.chessmates.repository

/**
 * Interface exposing query execution facilities
 */

interface QueryExecutor {
    void executeInsert(givenTableName, givenItems)
    def executeSelect(givenTableName, givenId)
}