package com.chessmates.utility

import spock.lang.Specification

import java.util.function.Predicate

class LichessResultSetTest extends Specification {

    private final neverStop = { false }

    def 'should add all page items to list until pages run out'() {
        given: 'ten pages of integers'
        def getPage = createGetPageFunction(10, 100)

        when:
        def resultSet = new LichessResultSet(getPage, neverStop)
        def results = resultSet.get()

        then: 'result should contain all pages'
        results == 1..1000
    }

    def 'should add all pages up to but not including failed predicate'() {
        given: 'ten pages of integers'
        def getPage = createGetPageFunction(10, 100)
        def stopOn80 = createStopPredicate(80)

        when:
        def resultSet = new LichessResultSet(getPage, stopOn80)
        def results = resultSet.get()

        then: 'result should include all up to 80'
        results == 1..79
    }

    def 'returns empty list for empty results page'() {
        given:
        def getPage = createEmptyPageFunction()

        when:
        def resultSet = new LichessResultSet(getPage, neverStop)
        def results = resultSet.get()

        then:
        results == []
    }

    /** Returns a predicate that will return true when given `numberToStop` */
    Predicate<Integer> createStopPredicate(Integer numberToStop) {
        return { Integer input -> input == numberToStop }
    }

    /** Returns an implementation of GetPageFunction that simulates no pages. */
    LichessResultSet.GetPageFunction<Integer> createEmptyPageFunction() {
        return (LichessResultSet.GetPageFunction<Integer>){ Integer pageNum -> new LichessResultPage<Integer>(
                currentPage: 1,
                previousPage: null,
                nextPage: null,
                numPages: 1,
                totalResults: 0,
                results: []
        )}
    }

    /** Returns an implementation of GetPageFunction. It simulates a result set of a given number of pages, with a given page size. */
    LichessResultSet.GetPageFunction<Integer> createGetPageFunction(Integer totalPages = 1, Integer pageSize = 100) {
        return (LichessResultSet.GetPageFunction<Integer>){ Integer pageNum ->

            def nextPage = pageNum < totalPages ? pageNum + 1 : null
            def previousPage = pageNum > 1 ? pageNum - 1 : null
            def totalResults = totalPages * pageSize

            def pageFrom = ((pageNum-1) * pageSize) + 1
            def pageTo = (pageNum* pageSize)
            def pageResults = pageFrom..pageTo

            return new LichessResultPage<Integer>(
                currentPage: pageNum,
                previousPage: previousPage,
                nextPage: nextPage,
                numPages: totalPages,
                totalResults: totalResults,
                results: pageResults
            )
        }
    }


}
