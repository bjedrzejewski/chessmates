package com.chessmates.controller

import com.chessmates.service.EntityService
import com.chessmates.model.Game
import com.chessmates.model.Player
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for returning information about Lichess Entities.
 */
@RestController
class EntityController {

    @Autowired
    EntityService entityService

    @GetMapping(value = 'players')
    List<Player> getPlayers() { entityService.getPlayers() }

    @GetMapping(value = 'games')
    List<Game> getGames() { entityService.getGames() }

}
