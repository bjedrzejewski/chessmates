package com.chessmates.utility

/**
 * Represents a page of results from the Lichess API.
 */
class LichessResultPage<T> {

    Integer currentPage
    Integer previousPage
    Integer nextPage
    Integer numPages
    Integer totalResults
    List<T> results

}
