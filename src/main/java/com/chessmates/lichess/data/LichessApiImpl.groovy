package com.chessmates.lichess.data

import com.chessmates.utility.HttpUtility
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Lichess Api.
 */
@Component
class LichessApiImpl implements LichessApi {

    private static final Logger logger = LoggerFactory.getLogger(LichessApiImpl)
    private static final String LICHESS_API_TEMPLATE = "https://en.lichess.org/api"

    @Autowired
    HttpUtility httpUtility

    @Override
    getPlayersPage(String teamId, int pageNumber, int pageSize) {
        logger.debug "Getting players in team: ${teamId} page: ${pageNumber}"

        def url = "${LICHESS_API_TEMPLATE}/user?team=${teamId}&nb=${pageSize}&page=${pageNumber}"

        def json = httpUtility.get(url)
        def paginatedResponse = new JsonSlurper().parseText(json)

        // Team API nests paginated response inside of a paginator field.
        paginatedResponse?.paginator
    }

    @Override
    getGamesPage(String playerId, int pageNumber, int pageSize ) {
        logger.debug "Getting games for player: ${playerId} page: ${pageNumber}"

        def url = "${LICHESS_API_TEMPLATE}/user/${playerId}/games?nb=${pageSize}&page=${pageNumber}"

        def json = httpUtility.get(url)
        new JsonSlurper().parseText(json)
    }

    @Override
    getGamesPage(String playerId, String opponentId, int pageNumber, int pageSize) {
        logger.debug "Getting games for player: ${playerId} opponent: ${opponentId} page: ${pageNumber}"

        def url = "${LICHESS_API_TEMPLATE}/games/vs/${playerId}/${opponentId}?nb=${pageSize}&page=${pageNumber}"

        def json = httpUtility.get(url)
        new JsonSlurper().parseText(json)
    }

}
