package com.chessmates.service

import com.chessmates.model.Game
import com.chessmates.model.Player
import com.chessmates.utility.LichessApi
import com.chessmates.utility.LichessResultPage
import org.apache.commons.lang3.tuple.ImmutablePair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * Service handling data requests relating to Lichess entities.
 */
@Service
class EntityServiceImpl implements EntityService {

    static final String TEAM_NAME = 'scott-logic'

    private LichessApi lichessApi
    private Map<String, Game> playerIdToLatestGame = new HashMap<>() // A mapping of player IDs to their latest process game.

    @Autowired
    EntityServiceImpl(LichessApi lichessApi) {
        this.lichessApi = lichessApi
    }

    /**
     * All SL players.
     */
    @Override
    List<Player> getPlayers() {
        def fetchPlayerPage = { String teamId, int pageNum ->
            lichessApi.getPlayers(teamId, pageNum) }

        def neverStop = { t -> false}

        getItemsFromPages(fetchPlayerPage.curry(TEAM_NAME), neverStop)
    }

    /**
     * Get all games between scott logic players.
     */
    @Override
    List<Game> getAllTeamGames() { getNewGames(null) }

    /**
     * Get new games between scott logic players up to the latest game we have already fetched.
     */
    List<Game> getGamesUntilLatestFetched() { getNewGames() }

    /**
     * Get new games between scott logic players, up to an already known game.
     */
    private List<Game> getNewGames() {
        def players = getPlayers()
        def playerCombinations = uniqueCombinations(players)

        def fetchGamePage = { Player player, Player opponent, int pageNum ->
            lichessApi.getGames(player.id, opponent.id, pageNum) }

        def gameIsLatest = { Player player, Player opponent, Game thisGame -> thisGame == getLatestGameForOpponents(player, opponent) }

        def games = playerCombinations.stream()
            // Get all of the games for a pair of players.
            .map { playerCombination ->
                def player = playerCombination.left
                def opponent = playerCombination.right

                /* So functional! Bind the fetchGamePage * stop condition with the player arguments. The getAllPages func isn't concerned
                with any arguments. */
                def gamesForOpponents = getItemsFromPages(
                        fetchGamePage.curry(player, opponent),
                        gameIsLatest.curry(player, opponent)
                )

                def latestGame = (gamesForOpponents.size()) ? gamesForOpponents.first() : null

                // Store the latest game processed for this player combination.
                putLatestGameForOpponents(player, opponent, latestGame)

                return gamesForOpponents
            }
            .flatMap { gamePageResults -> gamePageResults.stream() }
            .collect(Collectors.toList())

        return games
    }

    /**
     * Given a function that fetches the first page of Lichess results, and every following page, until the stop condition
     * is met, or there are no more pages.
     */
    static private <T> List<T> getItemsFromPages(Function<Integer, LichessResultPage<T>> fetchPage, Predicate<T> stopCondition) {
        def items = []

        // Start at page one and keep requesting pages until there are no more pages.
        def previousPage = fetchPage.apply(1)
        items.addAll(getItemsFromPage(previousPage, stopCondition)['items'])

        // Loop around all following pages until the stop condition is fulfilled or we hit the end of the results.
        while(previousPage?.nextPage != null) {

            def page = fetchPage.apply(previousPage.nextPage)
            def pageResults = getItemsFromPage(page, stopCondition)

            items.addAll(pageResults['items'])

            if (pageResults['stoppedEarly']) {
                return items
            }
        }

        return items
    }

    private getLatestGameForOpponents(Player player, Player opponent) {
        playerIdToLatestGame.get(player.id + opponent.id)
    }

    private putLatestGameForOpponents(Player player, Player opponent, Game game) {
        playerIdToLatestGame.putIfAbsent(player.id + opponent.id, game)
    }

    /**
     * Gets all results from a results page, starting from the first, stopping early if the stop condition is met.
     * The results are returned in a Map, with the page items stored in `items`, and a boolean `stoppedEarly` indicating
     * whether the `stopCondition` was met.
     */
    static private <T> Map getItemsFromPage(LichessResultPage<T> page, Predicate stopCondition) {
        def pageItems = []
        def iterator = page.results.iterator()
        T item
        while ((item = iterator[0])) {

            if (stopCondition.test(item)) {
                return [items: pageItems, stoppedEarly: true]
            }

            pageItems << item
        }
        return [items: pageItems, stoppedEarly: false]

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
