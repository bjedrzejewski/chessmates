package com.chessmates.controller

import com.chessmates.service.EntityService
import com.chessmates.model.Game
import com.chessmates.model.Player
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for returning information about Lichess Entities.
 */
@RestController
@CrossOrigin
class EntityController {

    @Autowired
    EntityService entityService

    /**
     * Return a list of all team players
     */
    @GetMapping(value = 'players')
    List<Player> getPlayers() { entityService.getPlayers() }

    /**
     * Return a list of all unique games played between all team members.
     */
    @GetMapping(value = 'games')
    List<Game> getGames() { entityService.getAllTeamGames() }


    // TODO: Temporarily here in the controller. This will eventually be ran as part of a scheduled job.
    @GetMapping(value = 'latestGames')
    List<Game> getLatestGames() { entityService.getGamesUntilLatestFetched() }

}
