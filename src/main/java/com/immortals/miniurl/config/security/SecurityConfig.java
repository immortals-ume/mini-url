package com.immortals.miniurl.config.security;

import com.immortals.miniurl.security.filter.JwtAuthorizationFilter;
import com.immortals.miniurl.security.jwt.AuthEntryPointJwt;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static com.immortals.miniurl.constants.UrlConstants.MAX_AGE_CORS_SECS;
import static com.immortals.miniurl.constants.UrlConstants.MAX_AGE_HSTS_SECS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthFilter;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(corsProperties.getAllowCredentials());
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setMaxAge(corsProperties.getMaxAge() != null ? corsProperties.getMaxAge() : MAX_AGE_CORS_SECS);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/redirect",
                                "/health",
                                "/actuator/**",
                                "/static/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.contentSecurityPolicy(csp -> csp
                                        .policyDirectives("default-src 'self'; script-src 'self' cdn.example.com; object-src 'none';")
                                )
                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                                .xssProtection(HeadersConfigurer.XXssConfig::disable)
                                .httpStrictTransportSecurity(hsts -> hsts
                                        .includeSubDomains(Boolean.TRUE)
                                        .maxAgeInSeconds(MAX_AGE_HSTS_SECS))
                )
                .requiresChannel(channel -> channel.anyRequest()
                        .requiresSecure())
                .build();
    }
}
