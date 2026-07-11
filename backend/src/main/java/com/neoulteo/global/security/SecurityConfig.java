package com.neoulteo.global.security;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(ObjectMapper objectMapper, JwtTokenProvider jwtTokenProvider,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.objectMapper = objectMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/plans", "/api/hotplaces/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/", "/api/attractions", "/api/attractions/**", "/api/hotplaces",
                        "/api/plans/shared/**",
                        "/api/hotplaces/popular", "/api/hotplaces/popular/**",
                        "/api/posts", "/api/posts/**",
                        "/uploads/community/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout",
                        "/api/users/signup", "/api/users/password-reset", "/app/ai/chat",
                        "/app/ai/smart-travel-chat", "/app/ai/evaluate-plan",
                        "/app/ai/travel-assistant").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/plans", "/api/hotplaces", "/api/batch/tour").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/plans/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/plans/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/hotplaces/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/hotplaces/**").authenticated()
                .anyRequest().authenticated());

        http.formLogin(login -> login
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("email")
                .passwordParameter("pw")
                .successHandler((request, response, authentication) -> {
                    CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
                    writeJson(response, userResponse(details, null, jwtTokenProvider.createToken(details)));
                })
                .failureHandler((request, response, exception) ->
                        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                        message(false, "Email or password does not match."))));

        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .logoutSuccessHandler((request, response, authentication) ->
                        writeJson(response, message(true, "Logged out."))));

        http.rememberMe(remember -> remember.disable());

        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) ->
                        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                                message(false, "Login is required."))));

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(java.util.List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private Map<String, Object> userResponse(CustomUserDetails details, String message) {
        return userResponse(details, message, null);
    }

    private Map<String, Object> userResponse(CustomUserDetails details, String message, String token) {
        Map<String, Object> body = message(true, message);
        body.put("id", details.getUser().getId());
        body.put("name", details.getUser().getName());
        body.put("email", details.getUser().getEmail());
        if (token != null) {
            body.put("token", token);
            body.put("tokenType", "Bearer");
        }
        return body;
    }

    private Map<String, Object> message(boolean success, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", success);
        if (message != null) {
            body.put("message", message);
        }
        return body;
    }

    private void writeJson(HttpServletResponse response, Map<String, Object> body) throws IOException {
        writeJson(response, HttpServletResponse.SC_OK, body);
    }

    private void writeJson(HttpServletResponse response, int status, Map<String, Object> body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
