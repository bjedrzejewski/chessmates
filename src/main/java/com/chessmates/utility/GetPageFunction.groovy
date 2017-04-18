package com.chessmates.utility

import com.chessmates.lichess.data.LichessResultPage

import java.util.function.Function

/**
 * Interface representing a function that takes a page number and returns a page of Lichess results.
 */
interface GetPageFunction<T> extends Function<Integer, LichessResultPage<T>> {}
