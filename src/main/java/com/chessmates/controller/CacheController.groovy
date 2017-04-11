package com.chessmates.controller

import com.chessmates.Application
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoints for controlling the cache. These are for temporary development purposes only.
 */
@RestController
class CacheController {

    private static final Logger logger = LoggerFactory.getLogger(CacheController)

    CacheManager cacheManager

    @Autowired
    CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager
    }

    /**
     * Clear all items in the cache.
     */
    @GetMapping(value = "clearCache")
    void clearCache() {
        logger.debug "Manually clearing cache in response to 'clearCache' REST endpoint hit"
        cacheManager.getCache(Application.REQUEST_CACHE_NAME).clear()
    }

}
