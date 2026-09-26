package com.example.marketdata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class CorsConfig {
 @Bean CorsWebFilter corsWebFilter() {
   CorsConfiguration c = new CorsConfiguration(); c.setAllowedOriginPatterns(List.of("http://localhost:*"));
   c.setAllowedMethods(List.of("GET","POST","OPTIONS")); c.setAllowedHeaders(List.of("*"));
   UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/**", c); return new CorsWebFilter(s);
 }
}
