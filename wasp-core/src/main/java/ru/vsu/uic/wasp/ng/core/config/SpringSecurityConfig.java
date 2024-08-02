package ru.vsu.uic.wasp.ng.core.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.vsu.uic.wasp.ng.core.service.UserService;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Autowired
    private UserService userService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->
                        auth.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/", "/static/**", "/actuator/**").permitAll()
                        .requestMatchers("/home").authenticated()
                        .requestMatchers("/repos/**").hasRole("REPO_MANAGER")
                        .requestMatchers("/users/**").hasRole("USER_MANAGER")
                        .requestMatchers("/cms/**").hasRole("CONTENT_MANAGER")
                        .requestMatchers("/account/**").authenticated()
                        .requestMatchers("/logout").authenticated()
                        .anyRequest().denyAll()
                )
                .formLogin(form -> {
                    form
                       .loginPage("/login")
                       .successForwardUrl("/home")
                       .failureForwardUrl("/login-failed")
                       .permitAll();
                })
                .logout(logout -> {
                    logout.deleteCookies("JSESSIONID");
                });
        return http.build();

    }

}
