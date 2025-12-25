package aletheia.project.Aletheia.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String token = null;
        String header = request.getHeader("Authorization");

        // 1. Check Header
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } 
        // 2. Check Cookies (for Thymeleaf/Browser navigation)
        if (token == null && request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if ("jwt".equals(cookie.getName())) {
                        token = cookie.getValue();
                    }
                }
            }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Wrap this in try-catch to prevent 500 Errors when token expires
                String username = jwtUtil.extractUsername(token);
                
                if (username != null) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    if (jwtUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                // Log error if needed, but DO NOT crash. 
                // Just let the request proceed as "anonymous", so SecurityConfig sends them to login.
                System.out.println("JWT Validation failed: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
