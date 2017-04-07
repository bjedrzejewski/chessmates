package com.chessmates.service

import com.chessmates.model.Game
import com.chessmates.model.Player
import com.chessmates.utility.LichessApi
import groovy.transform.TypeChecked
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

    @Autowired
    LichessApi lichessApi

    @Override
    List<Player> getPlayers() {
        lichessApi.getPlayers TEAM_NAME
    }

    @Override
    List<Game> getGames() {
        def players = lichessApi.getPlayers TEAM_NAME

        def scottLogicIds = players.stream()
                .map { player -> player.username }
                .collect(Collectors.toList())

        // Get all unique games played between players.
        players.stream()
                .map { player -> lichessApi.getGames player.id }
                .flatMap { games -> games.stream() }
                .distinct()
                .filter { Game game -> scottLogicIds.containsAll(game.players.values()) }
                .collect(Collectors.toList())
    }
}
