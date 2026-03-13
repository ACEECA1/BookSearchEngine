package com.app.demo.filter;

import com.app.demo.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public AuthFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        Cookie cookie = WebUtils.getCookie(request, "authToken");
        
        if (cookie != null) {
            String token = cookie.getValue();
            try {
                String email = jwtUtil.extractEmail(token);
                
                if (email != null && jwtUtil.isTokenValid(token, email)) {
                    // 1. Get roles and map them to Spring's authority type
                    List<SimpleGrantedAuthority> authorities = jwtUtil.extractRoles(token).stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                    // 2. Create the authentication object (Principal, Credentials, Authorities)
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(email, null, authorities);

                    // 3. Optional: Add request details (like IP address) to the token
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 4. "Sign in" the user for this specific request
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    Integer userId = jwtUtil.extractUserId(token);
                    request.setAttribute("userId", userId);                    
                }
            } catch (Exception e) {
                logger.error("Could not set user authentication in security context", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}