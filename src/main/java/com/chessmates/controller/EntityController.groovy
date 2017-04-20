package com.chessmates.controller

import com.chessmates.service.EntityService
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
    List getPlayers() { entityService.getPlayers() }

    /**
     * Return a list of all unique games played between all team members.
     */
    @GetMapping(value = 'games')
    List getGames() { entityService.getGames() }

}
