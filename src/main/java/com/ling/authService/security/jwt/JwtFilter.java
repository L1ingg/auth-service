package com.ling.authService.security.jwt;

import com.ling.authService.security.jwt.exception.InvalidTokenException;
import com.ling.authService.security.jwt.exception.InvalidTokenTypeException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HEADER_NAME);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(BEARER_PREFIX.length());

            if (!StringUtils.hasText(jwt) || !jwt.contains(".")) {
                throw new InvalidTokenException("Malformed JWT token");
            }

            if (!jwtService.isAccessToken(jwt)) {
                throw new InvalidTokenTypeException("Token is not access token");
            }

            String username = jwtService.extractUserName(jwt);

            if (!StringUtils.hasText(username)) {
                throw new InvalidTokenException("Username is missing in token");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    throw new InvalidTokenException("Invalid or expired JWT token");
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            throw new InvalidTokenException("Token expired");

        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            throw new InvalidTokenException("Invalid JWT token");

        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            throw e;

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            throw new AuthenticationCredentialsNotFoundException("Authentication failed", e);
        }
    }
}