package com.chessmates.utility

import com.chessmates.model.GameColor
import com.chessmates.model.Game
import com.chessmates.model.Player
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.ParseException
import java.util.function.Function
import java.util.stream.Collectors

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
    LichessResultPage<Player> getPlayers(String teamId, int pageNumber, int pageSize) {
        logger.debug "Getting players in team: ${teamId} page: ${pageNumber}"

        def url = "${LICHESS_API_TEMPLATE}/user?team=${teamId}&nb=${pageSize}&page=${pageNumber}"

        def json = httpUtility.get(url)
        def paginatedResponse = new JsonSlurper().parseText(json)

        // Team API nests paginated response inside of a paginator field.
        paginatedResponse = paginatedResponse?.paginator

        parsePage(paginatedResponse, LichessApiImpl.&parsePlayer)
    }

    @Override
    LichessResultPage<Game> getGames(String playerId, int pageNumber, int pageSize ) {
        logger.debug "Getting games for player: ${playerId} page: ${pageNumber}"

        def url = "${LICHESS_API_TEMPLATE}/user/${playerId}/games?nb=${pageSize}&page=${pageNumber}"

        def json = httpUtility.get(url)
        def paginatedResponse = new JsonSlurper().parseText(json)

        parsePage(paginatedResponse, LichessApiImpl.&parseGame)
    }

    @Override
    LichessResultPage<Game> getGames(String playerId, String opponentId, int pageNumber, int pageSize) {
        logger.debug "Getting games for player: ${playerId} opponent: ${opponentId} page: ${pageNumber}"

        def url = "${LICHESS_API_TEMPLATE}/games/vs/${playerId}/${opponentId}?nb=${pageSize}&page=${pageNumber}"

        def json = httpUtility.get(url)
        def paginatedResponse = new JsonSlurper().parseText(json)

        parsePage(paginatedResponse, LichessApiImpl.&parseGame)
    }

    /**
     * This method parses a Lichess results page.
     *
     * @param A parsed JSON response (already parsed into Object form from text)
     * @param A parsing function that parses the result set into model objects
     * @return A LichessResultPage representing the returned page of results parsed into model objects
     */
    private static <T> LichessResultPage<T> parsePage(Object paginatedResponse, Function<Object, T> parse) {
        List<Object> resultObjects = paginatedResponse?.currentPageResults
        resultObjects = resultObjects ?: []

        final pageResults = []
        resultObjects.each {
            try {
                final item = parse.apply it
                pageResults.add item
            } catch (Exception e) {
                logger.error e.message
            }
        }

        new LichessResultPage<T>(
                (Integer)paginatedResponse.currentPage,
                (Integer)paginatedResponse.previousPage,
                (Integer)paginatedResponse.nextPage,
                (Integer)paginatedResponse.nbPages,
                (Integer)paginatedResponse.nbResults,
                pageResults
        )
    }

    private static Player parsePlayer(Object playerObject) throws IllegalArgumentException {
        final playerId = (String)playerObject?.id

        if (!playerId) {
            final message = "Unable to parse Object to Player: ${playerId}"
            logger.error message
            throw new IllegalArgumentException(message)
        }

        new Player(
                playerId,
                (String)playerObject?.username
        )
    }

    private static  Game parseGame(Object gameObject) throws IllegalArgumentException {
        final gameId = (String)gameObject?.id

        if (!gameId) {
            throw new IllegalArgumentException("Unable to parse Object to Game: ${gameObject}")
        }

        final playerMap = new HashMap<GameColor, String>()
        playerMap.put(GameColor.WHITE, (String)gameObject?.players?.white?.userId)
        playerMap.put(GameColor.BLACK, (String)gameObject?.players?.black?.userId)

        new Game(
                gameId,
                playerMap,
        )
    }

}
