package com.config;

import com.enums.Role;
import com.service.JwtService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException, java.io.IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JwtAuthFilter] No Authorization header or not Bearer. Header=" + authHeader);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            System.out.println("[JwtAuthFilter] Token invalid: " + token);
            filterChain.doFilter(request, response);
            return;
        }
        String username = jwtService.extractUsername(token);
        Role role = jwtService.extractRole(token);

        System.out.println(
                "[JwtAuthFilter] Token valid. username=" + username + ", roleClaim=" + role + ", token=" + token);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        System.out.println("[JwtAuthFilter] Granted authorities set: " + authorities);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null,
                authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
