package com.chessmates.utility

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

import javax.net.ssl.HttpsURLConnection
import java.time.LocalDateTime
import static java.time.temporal.ChronoUnit.*


/**
 * Http utility that throttles requests.
 */
@Component
class ThrottledHttpUtility implements HttpUtility, Cache {

    // TODO: Shouldn't be set here?
    Integer THROTTLE_MILLIS = 1000
    Integer COOLDOWN_TIME_MILLIS = 60000
    LocalDateTime lastRequest


    @Override
    @Cacheable("requests")
    String get(String targetUrl) {
        def now = LocalDateTime.now()

        if (lastRequest) {
            def millisSince = MILLIS.between(lastRequest, now)

            if (millisSince < THROTTLE_MILLIS) {
                println "REQUEST: throttled - waiting ${THROTTLE_MILLIS - millisSince} millis"
                // TODO: I know I know...
                Thread.sleep(THROTTLE_MILLIS - millisSince)
            }
        }

        getRequest targetUrl
    }

    private String getRequest(String targetUrl) {
        def connection

        try {
            // Build request
            def url = new URL(targetUrl)
            connection = (HttpsURLConnection)url.openConnection()
            connection.setRequestMethod 'GET'
            connection.setDoOutput true

            // Get response
            def is = connection.getInputStream()

            // Lichess API recommends wait time of 1 minutes after receiving 329 code.
            if (connection.getResponseCode() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                // TODO: I know I know...
                Thread.sleep(COOLDOWN_TIME_MILLIS)
            }

            def rd = new BufferedReader(new InputStreamReader(is))
            def response = new StringBuffer()

            def line
            while((line = rd.readLine()) != null) {
                response.append line
                response.append '\r'
            }
            rd.close()
            return response.toString()

        } catch (Exception e) {
            e.printStackTrace()
            return null
        } finally {

            if(connection != null) {
                connection.disconnect()
            }
            lastRequest = LocalDateTime.now()
        }
    }

    @Override
    @CacheEvict(value = "requests", allEntries = true)
    void evictCache() {
        // TODO: Replace with logging...
        println "Evicting cache"
    }
}
