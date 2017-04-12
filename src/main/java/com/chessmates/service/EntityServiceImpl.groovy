package com.chessmates.service

import com.chessmates.model.Game
import com.chessmates.model.Player
import com.chessmates.utility.LichessApi
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

    @Autowired
    EntityServiceImpl(LichessApi lichessApi) {
        this.lichessApi = lichessApi
    }

    @Override
    List<Player> getPlayers() {
        lichessApi.getPlayers(TEAM_NAME, 1).results
    }

    @Override
    List<Game> getAllTeamGames() {
        def players = getPlayers()
        def playerCombinations = uniqueCombinations(players)

        playerCombinations.stream()
            .map { playerCombination ->
                def player = playerCombination.left
                def opponent = playerCombination.right

                getGames(player, opponent)
            }
            .flatMap { gamePageResults -> gamePageResults.stream() }
            .collect(Collectors.toList())
    }

    /**
     * Get all games between two opponents.
     *
     * This method requests the full set of page results from the lichess API.
     */
    private List<Game> getGames(Player player, Player opponent) {
        def games = new ArrayList<>()

        // Inline closure to get a page of games and add the games to the results list.
        def getPageResultsAndAppend = { int pageNumber ->
            def page = lichessApi.getGames(player.id, opponent.id, pageNumber)
            games.addAll(page.results)
            return page
        }

        // Start at page one and keep requesting games until there are no more pages.
        def previousPage = getPageResultsAndAppend(1)
        while(previousPage?.nextPage != null) {
            previousPage = getPageResultsAndAppend(previousPage.nextPage)
        }

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

                combinations.push(new ImmutablePair(player, opponent))
            }
            matchingIndex++
        }
        return combinations
    }
}
