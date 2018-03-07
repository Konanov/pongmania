//package com.konanov.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.ProviderManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import static java.util.Collections.singletonList;
//
//@Configuration
//@RequiredArgsConstructor
//public class AuthenticationManagerConfig {
//
//    private final UserDetailsService userDetailsService;
//
//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager clientAuthenticationManager() {
//        DaoAuthenticationProvider clientAuthenticationProvider = new DaoAuthenticationProvider();
//        clientAuthenticationProvider.setUserDetailsService(userDetailsService);
//        clientAuthenticationProvider.setHideUserNotFoundExceptions(false);
//        clientAuthenticationProvider.setPasswordEncoder(passwordEncoder());
//        return new ProviderManager(singletonList(clientAuthenticationProvider));
//    }
//}
