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
        lichessApi.getPlayers TEAM_NAME
    }

    @Override
    List<Game> getGames() {
        def playerPageResult = lichessApi.getPlayers TEAM_NAME

        def scottLogicIds = playerPageResult.results.stream()
                .map { player -> player.username }
                .collect(Collectors.toList())

        // Get all unique games played between players.
        playerPageResult.results.stream()
                .map { player -> lichessApi.getGames player.id }
                .flatMap { gamePageResults -> gamePageResults.results.stream() }
                .distinct()
                .filter { Game game -> scottLogicIds.containsAll(game.players.values()) }
                .collect(Collectors.toList())
    }
}
