package com.chessmates.utility

/**
 * Represents a page of results from the Lichess API.
 */
class LichessResultPage<T> {

    final Integer currentPage
    final Integer previousPage
    final Integer nextPage
    final Integer numPages
    final Integer totalResults
    final List<T> results

    LichessResultPage(Integer currentPage, Integer previousPage, Integer nextPage, Integer numPages,
                      Integer totalResults, List<T> results) {
        this.currentPage = currentPage
        this.previousPage = previousPage
        this.nextPage = nextPage
        this.numPages = numPages
        this.totalResults = totalResults
        this.results = results
    }
}
