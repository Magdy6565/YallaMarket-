package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider //ignore Bean warning we will get to that
    ) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/auth/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authenticationProvider(authenticationProvider)
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable()) // Disable CSRF
            .authorizeHttpRequests(authorize -> authorize
                    // Permit authentication endpoints (login, signup, etc.)
                    .requestMatchers("/auth/**").permitAll()
                    
                    // Permit web resources and Thymeleaf views for login
                    .requestMatchers("/", "/profile","/register","/error","/vendor-orders/**" ,"/edit-product/**","/add-product" ,"/verify" ,"/login", "/css/**", "/js/**", "/images/**").permitAll()
                    // Permit all product API endpoints
                    .requestMatchers("/api/my-products/**").permitAll()
                    .requestMatchers("/api/vendor/**").permitAll()
                    .requestMatchers("/api/store/orders").permitAll()
                    // Permit store orders endpoints
                    .requestMatchers("/api/store/orders/**").permitAll()
                    .requestMatchers("/supermarket/**").permitAll()
                    // Permit refund endpoints
                    .requestMatchers("/api/refunds/**").permitAll()
                    .requestMatchers("/api/ratings/vendor/**").permitAll()

                    // Require authentication for products pages
                    .requestMatchers("/products", "/products/**").authenticated()
                    .requestMatchers("/users/**").authenticated()

                    // Require authentication for any other request not explicitly permitted above
                    .anyRequest().authenticated() // This is the default for everything else
            )            .formLogin(form -> form
                    .loginPage("/login")
                    .successHandler((request, response, authentication) -> {
                        Object principal = authentication.getPrincipal();
                        if (principal instanceof com.example.demo.model.User) {
                            com.example.demo.model.User user = (com.example.demo.model.User) principal;
                            if (user.getRole() == 0) {
                                // Admin redirect
                                response.sendRedirect(request.getContextPath() + "/admin/users");
                            } else if (user.getRole() == 2) {
                                // Retail store redirect
                                response.sendRedirect(request.getContextPath() + "/supermarket/home");
                            } else {
                                // Vendor or other roles
                                response.sendRedirect(request.getContextPath() + "/products");
                            }
                        } else {
                            response.sendRedirect(request.getContextPath() + "/products");
                        }
                    })
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            )
            // Configure session management
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            // Set the custom AuthenticationProvider
            .authenticationProvider(authenticationProvider)
            // Add the JWT filter before the standard UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://app-backend.com", "http://localhost:8080")); //TODO: update backend url
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}