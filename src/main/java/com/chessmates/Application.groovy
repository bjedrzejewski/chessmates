package com.chessmates

import com.chessmates.utility.DisableSSL
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

/**
 * This is the starting point of the application
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
class Application {

    /**
     * Main entry method for the application
     * @param args
     */
    static void main(String[] args) {
        DisableSSL.disableCertificatesValidation()
        SpringApplication.run(Application, args)
    }

    /**
     * Configuration for Spring MVC.
     */
    @Bean
    WebMvcConfigurer createWebConfigurer() {
        new WebMvcConfigurerAdapter() {

            /**
             * Enable CORS for all origins.
             */
            @Override
            void addCorsMappings(CorsRegistry registry) {
                registry.addMapping('/**')
            }
        }
    }

}
