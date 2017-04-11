package com.chessmates.service

import com.chessmates.model.Game
import com.chessmates.model.Player
import com.chessmates.utility.LichessApi
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
    List<Game> getGames() {
        def page = lichessApi.getPlayers TEAM_NAME, 1

        def scottLogicIds = page.results.stream()
                .map { player -> player.username }
                .collect(Collectors.toList())

        // Get all unique games played between players.
        page.results.stream()
                .map { player -> lichessApi.getGames(player.id, 1).results }
                .flatMap { gamePageResults -> gamePageResults.stream() }
                .distinct()
                .filter { Game game -> scottLogicIds.containsAll(game.players.values()) }
                .collect(Collectors.toList())
    }
}
