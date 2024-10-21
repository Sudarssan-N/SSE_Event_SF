package com.example.sse_server;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;



@Configuration
public class CorsConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/sse/**")
                .allowedOrigins("https://wise-hawk-hdi2dp-dev-ed.trailblaze.my.salesforce.com", "http://localhost:8080", "https://llama-upright-possibly.ngrok-free.app") // Update with specific origins in production
                .allowedMethods("GET")
                .allowedHeaders("*");
    }
}
