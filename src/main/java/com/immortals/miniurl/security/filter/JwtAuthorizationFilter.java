package com.immortals.miniurl.security.filter;

import com.immortals.miniurl.security.jwt.JwtUtil;
import com.immortals.miniurl.model.security.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.immortals.miniurl.constants.UrlConstants.HEADER_STRING;
import static com.immortals.miniurl.constants.UrlConstants.TOKEN_PREFIX;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            Jws<Claims> parsedToken = jwtUtil.validateToken(token);
            Claims claims = parsedToken.getBody();

            String username = jwtUtil.getUsername(claims);
            Long userId = claims.get("userId", Long.class);
            List<SimpleGrantedAuthority> authorities = jwtUtil.getRoles(claims)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();


            User principal = new User(userId, username, authorities);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
