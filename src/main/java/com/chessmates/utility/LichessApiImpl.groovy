package com.chessmates.utility

import com.chessmates.model.GameColor
import com.chessmates.model.Game
import com.chessmates.model.Player
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.function.Function
import java.util.stream.Collectors

/**
 * Lichess Api.
 */
@Component
class LichessApiImpl implements LichessApi {

    static String LICHESS_API_TEMPLATE = "https://en.lichess.org/api"
    static String PAGE_SIZE_PLAYERS = 50
    static String PAGE_SIZE_GAMES = 100

    private static final Logger logger = LoggerFactory.getLogger(LichessApiImpl)

    @Autowired
    HttpUtility httpUtility

    @Override
    LichessResultPage<Player> getPlayers(String teamId, int pageNumber) {
        logger.debug "Getting players in team: ${teamId} page: ${pageNumber}"

        // TODO: Handle multiple pages
        def url = "${LICHESS_API_TEMPLATE}/user?team=${teamId}&nb=${PAGE_SIZE_PLAYERS}&page=${pageNumber}"

        def json = httpUtility.get(url)
        def paginatedResponse = new JsonSlurper().parseText(json)

        // Team API nests paginated response inside of a paginator field.
        paginatedResponse = paginatedResponse?.paginator

        parsePage(paginatedResponse, LichessApiImpl.&parsePlayer)
    }

    @Override
    LichessResultPage<Game> getGames(String playerId, int pageNumber) {
        logger.debug "Getting games for player: ${playerId} page: ${pageNumber}"

        // TODO: Handle multiple pages
        def url = "${LICHESS_API_TEMPLATE}/user/${playerId}/games?nb=${PAGE_SIZE_GAMES}&page=${pageNumber}"

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

        def pageResults = resultObjects.stream()
                .map(parse)
                .collect(Collectors.toList())

        new LichessResultPage<T>(
                results: pageResults,
                previousPage: paginatedResponse.previousPage,
                currentPage: paginatedResponse.currentPage,
                nextPage: paginatedResponse.nextPage,
                numPages: paginatedResponse.numPages,
                totalResults: paginatedResponse.totalResults
        )
    }

    private static Player parsePlayer(Object playerObject) {
        new Player(
                id: playerObject?.id,
                username: playerObject?.username
        )
    }

    private static  Game parseGame(Object gameObject) {
        def playerMap = new HashMap<GameColor, String>()
        playerMap.put(GameColor.WHITE, (String)gameObject?.players?.white?.userId)
        playerMap.put(GameColor.BLACK, (String)gameObject?.players?.black?.userId)

        new Game(
                id: gameObject?.id,
                players: playerMap,
        )
    }

}
