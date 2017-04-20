package com.chessmates.lichess.data

import java.util.function.Function
import java.util.function.Predicate

/**
 * Class responsible for fetching a full lichess result set from multiple pages.
 */
class LichessResultSet {

    private final Function<Integer, Object> getPage
    private final Predicate shouldStop

    /**
     * The result set must be constructed with a set of closures defining certain aspects of its behaviour.
     *
     * @param getPage Get a new results page for a given page number.
     * @param shouldStop Called for every result in a page - return true to stop, otherwise result set will stop building all pages fetched.
     */
    LichessResultSet(Function<Integer, Object> getPage, Predicate shouldStop = { false }) {
        this.getPage = getPage
        this.shouldStop = shouldStop
    }

    /**
     * Get all pages for the given result set.
     */
    List get() {
        def items = []

        // Start at page one and keep requesting pages until there are no more pages.
        def currentPage = getPage.apply(1)

        // Loop around all following pages until the stop condition is fulfilled or we hit the end of the results.
        while(currentPage != null) {

            def pageResults = getItemsFromPage(currentPage, shouldStop)

            items.addAll(pageResults['items'])

            if (pageResults['stoppedEarly']) {
                return items
            }

            currentPage = currentPage.nextPage ? getPage.apply(currentPage.nextPage) : null
        }

        return items
    }

    /**
     * Gets all results from a results page, starting from the first, stopping early if the stop condition is met.
     * The results are returned in a Map, with the page items stored in `items`, and a boolean `stoppedEarly` indicating
     * whether the `stopCondition` was met.
     */
    static private Map getItemsFromPage(Object page, Predicate shouldStop) {
        def pageItems = []
        def iterator = page.currentPageResults.iterator()
        def item
        while ((item = iterator[0]) != null) {

            if (shouldStop.test(item)) {
                return [items: pageItems, stoppedEarly: true]
            }

            pageItems << item
        }
        return [items: pageItems, stoppedEarly: false]

    }

}
