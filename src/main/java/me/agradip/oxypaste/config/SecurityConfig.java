package me.agradip.oxypaste.config;

import me.agradip.oxypaste.filters.TokenFilter;
import me.agradip.oxypaste.service.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final TokenService tokenService;

    public SecurityConfig(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/user/token/**").authenticated() // Require authentication for this endpoint
                        .anyRequest().permitAll() // Allow all other requests without authentication
                )
                .addFilterBefore(new TokenFilter(tokenService), BasicAuthenticationFilter.class) // Add custom token filter
                .csrf(AbstractHttpConfigurer::disable); // Disable CSRF protection (optional, but recommended for APIs)

        return http.build();
    }
}