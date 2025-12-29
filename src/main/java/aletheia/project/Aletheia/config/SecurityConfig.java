package aletheia.project.Aletheia.config;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import aletheia.project.Aletheia.security.CustomUserDetailsService;
import aletheia.project.Aletheia.security.JwtFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtFilter jwtFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtFilter jwtFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        SecureRandom secureRandom;
        try{
            secureRandom = SecureRandom.getInstanceStrong();
        } catch(Exception e) {
            secureRandom = new SecureRandom();
        }
        return new BCryptPasswordEncoder(12, secureRandom);
    }

    @Bean
    public DaoAuthenticationProvider authProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    // In SecurityConfig.java
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .csrf(csrf -> csrf.disable()) // Enable this later for production security
            .cors(Customizer.withDefaults())
            
            .authenticationProvider(authProvider())
            .authorizeHttpRequests(auth -> auth
                // 1. Only allow these without login
                .requestMatchers("/login", "/register", "/login-process", "/register-process").permitAll()
                // 2. Role-Based Endpoints
                // Only Admins can access /admin/**
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Reviewers and Admins can access review management
                .requestMatchers("/reviews/**").hasAnyRole("REVIEWER", "ADMIN")
                
                // Researchers (and usually others) can access paper submission
                .requestMatchers("/paper-form", "/papers/**").hasAnyRole("RESEARCHER", "ADMIN")

                .anyRequest().authenticated()
            )
            
            .logout(logout -> logout
                .logoutUrl("/logout")                 // 1. Listen for POST /logout
                .logoutSuccessUrl("/login?logout")    // 2. Redirect to login with ?logout param
                .deleteCookies("jwt", "JSESSIONID")   // 3. Delete cookies
                .clearAuthentication(true)            // 4. Clear Security Context
                .permitAll()
            )
            // 3. Redirect to /login if the user is not authenticated
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/login");
                })
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

}

