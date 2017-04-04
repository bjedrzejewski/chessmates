package com.chessmates

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter


@SpringBootApplication
class Application {

    static void main(String[] args) {
        SpringApplication.run(Application, args)
    }

    @Bean
    WebMvcConfigurer corsConfigurer() {
        new WebMvcConfigurerAdapter() {
            @Override
            void addCorsMappings(CorsRegistry registry) {
                registry.addMapping('/**')
            }
        }
    }

}
