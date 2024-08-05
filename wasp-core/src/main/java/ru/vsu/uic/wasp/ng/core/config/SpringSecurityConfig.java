package ru.vsu.uic.wasp.ng.core.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.vsu.uic.wasp.ng.core.security.WaspAccessDeniedHandler;
import ru.vsu.uic.wasp.ng.core.security.WaspAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    private static final int BCRYPT_STRENGTH = 10;
    private final WaspAuthenticationProvider waspAuthenticationProvider;

    public SpringSecurityConfig(WaspAuthenticationProvider waspAuthenticationProvider) {
        this.waspAuthenticationProvider = waspAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptVersion.$2A, BCRYPT_STRENGTH);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        waspAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationManagerBuilder.authenticationProvider(waspAuthenticationProvider);

        return authenticationManagerBuilder.build();
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
                                .requestMatchers("/access-denied").authenticated()
                                .requestMatchers("/logout").authenticated()
                                .anyRequest().denyAll()
                )
                .exceptionHandling(eh -> {
                    eh.accessDeniedHandler(new WaspAccessDeniedHandler());
                })
                .formLogin(form -> form
                   // Should point to our controller returning the login form
                   .loginPage("/login")
                   // Any valid URL. Spring Security will use it for internal authentication logic.
                   // Access to the URL should be available for all (see the configuration above)!
                   .loginProcessingUrl("/auth")
                   // Should point to our controller returning the default home page for all authenticated users.
                   .successForwardUrl("/home")
                   // Should point to our controller returning the default page for login failure.
                   .failureForwardUrl("/login-failed")
                   .permitAll())
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
