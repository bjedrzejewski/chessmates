package com.chessmates

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

/**
 * This is the starting point of the application
 */
@SpringBootApplication
class Application {

    /**
     * Main entry method for the application
     * @param args
     */
    def static void main(String[] args) {
        SpringApplication.run(Application, args)
    }

}
