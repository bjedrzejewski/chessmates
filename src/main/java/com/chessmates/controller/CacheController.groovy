package com.chessmates.controller

import com.chessmates.utility.Cache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoints for controlling the cache. These are for temporary development purposes only.
 */
@RestController
class CacheController {

    Cache cache

    @Autowired
    CacheController(Cache cache) {
        this.cache = cache
    }

    /**
     * Clear all items in the cache.
     */
    @GetMapping(value = "clearCache")
    void clearCache() {
        cache.evictCache()
    }

}
