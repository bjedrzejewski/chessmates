package com.chessmates.repository

/**
 * Interface exposing query execution facilities
 */

interface QueryExecutor {
    void executeInsert(givenTableName, givenItems)
    List executeSelect(givenTableName)
    List executeSelect(givenTableName, givenId)
}