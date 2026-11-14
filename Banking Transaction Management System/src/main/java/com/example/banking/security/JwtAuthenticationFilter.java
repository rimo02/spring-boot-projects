package com.example.banking.security;

import com.example.banking.auth.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.*;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtDecoder decoder;
    private final UserRepository users;
    private final AuthenticationEntryPoint entryPoint;

    public JwtAuthenticationFilter(JwtDecoder decoder, UserRepository users, AuthenticationEntryPoint entryPoint) {
        this.decoder = decoder;
        this.users = users;
        this.entryPoint = entryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.startsWith("Bearer "))
                    throw new IllegalArgumentException();
                var jwt = decoder.decode(header.substring(7));
                var user = users.findById(UUID.fromString(jwt.getSubject())).orElseThrow(IllegalArgumentException::new);
                var auth = new UsernamePasswordAuthenticationToken(new Actor(user.getId(), user.getRole()), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                entryPoint.commence(request, response,
                        new org.springframework.security.authentication.BadCredentialsException("Invalid token"));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
