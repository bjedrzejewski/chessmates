package com.chessmates

import com.chessmates.utility.Cache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Class responsible for defining scheduled tasks.
 */
@Component
class Scheduler {

    Cache cache

    @Autowired
    Scheduler(Cache cache) {
        this.cache = cache
    }

    /**
     * Clear the cache every 30 minutes.
     */
    @Scheduled(fixedRate = 1800000L)
    void clearCache() {
        cache.evictCache()
    }
}
