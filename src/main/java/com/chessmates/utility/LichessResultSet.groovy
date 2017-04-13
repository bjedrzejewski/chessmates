package com.chessmates.utility

import java.util.function.Function
import java.util.function.Predicate

/**
 * Class responsible for fetching a full lichess result set from multiple pages.
 */
class LichessResultSet<T> {

    interface GetPageFunction<T> extends Function<Integer, LichessResultPage<T>> {}

    private GetPageFunction<T> getPage
    private Predicate<T> shouldStop


    /**
     * The result set must be constructed with a set of closures defining certain aspects of its behaviour.
     *
     * @param getPage Get a new results page for a given page number.
     * @param shouldStop Called for every result in a page - return true to stop, otherwise result set will stop building all pages fetched.
     */
    LichessResultSet(GetPageFunction<T> getPage, Predicate<T> shouldStop) {
        this.getPage = getPage
        this.shouldStop = shouldStop
    }

    /**
     * Get all pages for the given result set.
     */
    List<T> get() {
        def items = []

        // Start at page one and keep requesting pages until there are no more pages.
        def previousPage = getPage.apply(1)
        items.addAll(getItemsFromPage(previousPage, shouldStop)['items'])

        // Loop around all following pages until the stop condition is fulfilled or we hit the end of the results.
        while(previousPage?.nextPage != null) {

            def page = getPage.apply(previousPage.nextPage)
            def pageResults = getItemsFromPage(page, shouldStop)

            items.addAll(pageResults['items'])

            if (pageResults['stoppedEarly']) {
                return items
            }
        }

        return items
    }

    /**
     * Gets all results from a results page, starting from the first, stopping early if the stop condition is met.
     * The results are returned in a Map, with the page items stored in `items`, and a boolean `stoppedEarly` indicating
     * whether the `stopCondition` was met.
     */
    static private <T> Map getItemsFromPage(LichessResultPage<T> page, Predicate<T> shouldStop) {
        def pageItems = []
        def iterator = page.results.iterator()
        T item
        while ((item = iterator[0])) {

            if (shouldStop.test(item)) {
                return [items: pageItems, stoppedEarly: true]
            }

            pageItems << item
        }
        return [items: pageItems, stoppedEarly: false]

    }

}
