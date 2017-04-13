package com.chessmates.service

import com.chessmates.model.Game
import com.chessmates.model.Player
import com.chessmates.utility.LichessApi
import com.chessmates.utility.LichessResultSet
import org.apache.commons.lang3.tuple.ImmutablePair

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.stream.Collectors

/**
 * Service handling data requests relating to Lichess entities.
 */
@Service
class EntityServiceImpl implements EntityService {

    static final String TEAM_NAME = 'scott-logic'

    private LichessApi lichessApi
    // TODO: Temporary - this wants to come from the database.
    private Map<ImmutablePair, Game> playerIdToLatestGame = new HashMap<>() // A mapping of player IDs to their latest process game.

    @Autowired
    EntityServiceImpl(LichessApi lichessApi) {
        this.lichessApi = lichessApi
    }

    /**
     * All SL players.
     */
    @Override
    List<Player> getPlayers() {
        def fetchPlayerPage = { String teamId, int pageNum -> lichessApi.getPlayers(teamId, pageNum) }

        def neverStop = { t -> false}

        def resultSet = new LichessResultSet<Player>(
                (LichessResultSet.GetPageFunction)fetchPlayerPage.curry(TEAM_NAME),
                neverStop
        )

        resultSet.get()
    }

    /**
     * Get all games between scott logic players.
     */
    @Override
    List<Game> getAllTeamGames() {
        getGames(getPlayers())
    }

    /**
     * Get new games between scott logic players.
     */
    List<Game> getGamesUntilLatestFetched() { getGames(getPlayers(), playerIdToLatestGame) }

    /**
     * Get only new games for each individual player combination.
     *
     * If no map is provided all historical games will be fetched. Go get a coffee!
     *
     * @param players The players to get new games for.
     * @param latestGameMap A map containing the latest game for each pair of opponents.
     * @return A list of all new games.
     */
    private List<Game> getGames(List<Player> players, Map<ImmutablePair, Game> latestGameMap = new HashMap<>()) {

        def fetchGamePage = { Player player, Player opponent, int pageNum -> lichessApi.getGames(player.id, opponent.id, pageNum) }

        def gameIsLatest = { Player player, Player opponent, Game thisGame ->
            thisGame == latestGameMap.get(new ImmutablePair(player, opponent))
        }

        def playerCombinations = uniqueCombinations(players)

        def games = playerCombinations.stream()
            // Get all of the games for a pair of players.
            .map { playerCombination ->

                def player = playerCombination.left
                def opponent = playerCombination.right

                def resultSet = new LichessResultSet<Game>(
                        /* So functional! Bind the fetchGamePage * stop condition with the player arguments. The getAllPages func isn't concerned
                        with any arguments. */
                        /* PS - groovy closures! Why don't you play nice with Java 8 functions?! */
                        (LichessResultSet.GetPageFunction)fetchGamePage.curry(player, opponent),
                        gameIsLatest.curry(player, opponent),
                )

                def games = resultSet.get()

                if (games.size()) {
                    latestGameMap.putIfAbsent(playerCombination, games.first())
                }

                return games
            }
            .flatMap { gamePageResults -> gamePageResults.stream() }
            .collect(Collectors.toList())

        return games
    }


    /**
     * Given an array of objects, return a list of all the unique combination between different objects.
     */
    static private <T> List<ImmutablePair<T, T>> uniqueCombinations(List<T> objects) {
        def matchingIndex = 1
        def combinations = []
        def numPlayers = objects.size()

        for (def playerIndex = 0; playerIndex<numPlayers; playerIndex++) {
            for (def opponentIndex = matchingIndex; opponentIndex<numPlayers; opponentIndex++) {

                def player = objects.get(playerIndex)
                def opponent = objects.get(opponentIndex)

                combinations.add(new ImmutablePair(player, opponent))
            }
            matchingIndex++
        }
        return combinations
    }
}
