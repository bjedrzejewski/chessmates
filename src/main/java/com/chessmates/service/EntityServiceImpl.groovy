package com.chessmates.service

import com.chessmates.model.Game
import com.chessmates.model.Player
import com.chessmates.utility.LichessApi
import com.chessmates.utility.LichessResultPage
import org.apache.commons.lang3.tuple.ImmutablePair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.function.Function
import java.util.stream.Collectors

/**
 * Service handling data requests relating to Lichess entities.
 */
@Service
class EntityServiceImpl implements EntityService {

    static final String TEAM_NAME = 'scott-logic'

    private LichessApi lichessApi

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

        getAllPages(fetchPlayerPage.curry(TEAM_NAME))
    }

    /**
     * Get all games between scott logic players.
     */
    @Override
    List<Game> getAllTeamGames() {
        def players = getPlayers()
        def playerCombinations = uniqueCombinations(players)

        def fetchGamePage = { Player player, Player opponent, int pageNum ->
            lichessApi.getGames(player.id, opponent.id, pageNum) }

        playerCombinations.stream()
            // Get all of the games for a pair of players.
            .map { playerCombination ->
                def player = playerCombination.left
                def opponent = playerCombination.right

                /* So functional! Bind the fetchGamePage with the player arguments. The getAllPages func isn't concerned
                with any arguments other than pages. */
                getAllPages(fetchGamePage.curry(player, opponent))
            }
            .flatMap { gamePageResults -> gamePageResults.stream() }
            .collect(Collectors.toList())
    }

    /**
     * Given a function that fetches a page of Lichess results, return every following page and return the results as
     * a list.
     */
    static private <T> List<T> getAllPages(Function<Integer, LichessResultPage<T>> fetchPage) {
        def items = []

        // Inline closure to get a page of items and add the games to the results list.
        def fetchPageResultAndAppend = { int pageNumber ->
            def page = fetchPage.apply(pageNumber)
            items.addAll(page.results)
            return page
        }

        // Start at page one and keep requesting pages until there are no more pages.
        def previousPage = fetchPageResultAndAppend(1)
        while(previousPage?.nextPage != null) {
            previousPage = fetchPageResultAndAppend(previousPage.nextPage)
        }

        return items
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

                combinations.push(new ImmutablePair(player, opponent))
            }
            matchingIndex++
        }
        return combinations
    }
}
