package com.immortals.miniurl.security.filter;

import com.immortals.miniurl.model.security.User;
import com.immortals.miniurl.security.jwt.JwtProvider;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static com.immortals.miniurl.constants.UrlConstants.HEADER_STRING;
import static com.immortals.miniurl.constants.UrlConstants.TOKEN_PREFIX;

@Order(2)
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthorizationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            boolean validated = jwtProvider.validateToken(token);
            if (validated) {


                String username = jwtProvider.getUsernameFromToken(token);

                List<SimpleGrantedAuthority> authorities = jwtProvider.getAuthoritiesFromToken(token)
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                JWTClaimsSet claims = jwtProvider.getClaimsFromToken(token);
                Long userId = jwtProvider.getUserIdFromClaims(claims, "userId");

                User principal = new User(userId, username, authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        filterChain.doFilter(request, response);
    }
}
