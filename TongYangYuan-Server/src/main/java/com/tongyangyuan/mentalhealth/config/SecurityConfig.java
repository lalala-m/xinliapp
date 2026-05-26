package com.tongyangyuan.mentalhealth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/auth/**",
                        "/openim/**",
                        "/ws/**",
                        "/stomp",
                        "/stomp/**",
                        "/api/stomp",
                        "/api/stomp/**",
                        "/error",
                        "/consultants/**",
                        "/appointments/**",
                        "/learning/**",
                        "/messages/**",
                        "/uploads/**",
                        "/upload/**",
                        "/home/config",
                        "/home/admin/**",
                        "/api/call/**",
                        "/user/info",
                        "/v3/api-docs/",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/ws_test.html",
                        "/websocket_test.html",
                        "/video-signaling",
                        "/video-signaling/**",
                        "/api/video-signaling",
                        "/api/video-signaling/**",
                        // PDF endpoints
                        "/api/consultation/pdf/**"
                ).permitAll()
                .requestMatchers("/admin/**").authenticated() // Explicitly require authentication for admin
                .anyRequest().permitAll() // 允许所有其他请求（JWT过滤器会单独处理认证）
            );

        http.addFilterBefore(jwtRequestFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @org.springframework.beans.factory.annotation.Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Consider restricting this in production
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
