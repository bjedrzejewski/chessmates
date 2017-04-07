package com.chessmates.utility

import com.chessmates.model.GameColor
import com.chessmates.model.Game
import com.chessmates.model.Player
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.stream.Collectors

/**
 * Lichess Api.
 */
@Component
class LichessApiImpl implements LichessApi {

    static String LICHESS_API_TEMPLATE = "https://en.lichess.org/api"
    static String PAGE_SIZE_PLAYERS = 50
    static String PAGE_SIZE_GAMES = 100
    static String STARTING_PAGE = 1

    @Autowired
    HttpUtility utility

    @Override
    List<Player> getPlayers(String teamId) {
        // TODO: Handle multiple pages
        def url = "${LICHESS_API_TEMPLATE}/user?team=${teamId}&nb=${PAGE_SIZE_PLAYERS}&page=${STARTING_PAGE}"

        // TODO: Implement real logging.
        println "REQUEST: players for team ${teamId}"

        def json = utility.get(url)

        def jsonSlurper = new JsonSlurper()
        def paginatedResponse = jsonSlurper.parseText(json)

        List<Object> playerObjects = paginatedResponse?.paginator?.currentPageResults

        return playerObjects.stream()
                .map { playerObject -> parsePlayer(playerObject) }
                .collect(Collectors.toList())
    }

    @Override
    List<Game> getGames(String playerId) {
        // TODO: Handle multiple pages
        def url = "${LICHESS_API_TEMPLATE}/user/${playerId}/games?nb=${PAGE_SIZE_GAMES}&page=${STARTING_PAGE}"

        println "REQUEST: games for player ${playerId}"

        def json = utility.get(url)

        def jsonSlurper = new JsonSlurper()
        def paginatedResponse = jsonSlurper.parseText(json)

        // Doesn't have the paginator field in the response for games, unlike players response
        List<Object> gameObjects = paginatedResponse?.currentPageResults

        return gameObjects.stream()
                .map { gameObject -> parseGame(gameObject) }
                .collect(Collectors.toList())
    }

    private Player parsePlayer(Object playerObject) {
        new Player(
                id: playerObject?.id,
                username: playerObject?.username
        )
    }

    private Game parseGame(Object gameObject) {
        def playerMap = new HashMap<GameColor, String>()
        playerMap.put(GameColor.WHITE, (String)gameObject?.players?.white?.userId)
        playerMap.put(GameColor.BLACK, (String)gameObject?.players?.black?.userId)

        new Game(
                id: gameObject?.id,
                players: playerMap,
        )
    }

}
