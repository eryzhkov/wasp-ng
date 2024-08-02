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
import org.springframework.web.filter.CommonsRequestLoggingFilter;
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
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter
                = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        filter.setAfterMessagePrefix("After request: ");
        return filter;
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
                        .requestMatchers("/", "/auth", "/static/**", "/actuator/**").permitAll()
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
                       // Should point to our controller returning the login form
                       .loginPage("/login")
                       // Any valid URL. Spring Security will use it for internal authentication logic.
                       // Access to the URL should be available for all (see the configuration above)!
                       .loginProcessingUrl("/auth")
                       // Should point to our controller returning the default home page for all authenticated users.
                       .successForwardUrl("/home")
                       // Should point to our controller returning the default page for login failure.
                       .failureForwardUrl("/login-failed")
                       .permitAll();
                })
                .logout(logout -> {
                    logout
                       // Should point to our controller returning the default public page.
                       .logoutSuccessUrl("/")
                       .invalidateHttpSession(true)
                       .clearAuthentication(true)
                       .deleteCookies("JSESSIONID");
                });
        return http.build();

    }

}
