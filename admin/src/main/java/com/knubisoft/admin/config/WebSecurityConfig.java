package com.knubisoft.admin.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.UUID;

@Configuration
@EnableWebSecurity
@Import(AdminServerProperties.class)
public class WebSecurityConfig {
    private final AdminServerProperties adminServer;

    public WebSecurityConfig(AdminServerProperties adminServer) { this.adminServer = adminServer; }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminServer.getContextPath() + "/");

        http.authorizeHttpRequests()
                .requestMatchers(adminServer.getContextPath() + "/assets/**").permitAll()
                .requestMatchers(adminServer.getContextPath() + "/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage(adminServer.getContextPath() + "/login")
                .successHandler(successHandler)
                .and()
                .logout()
                .logoutUrl(adminServer.getContextPath() + "/logout")
                .and()
                .httpBasic()//при попытке доступа к любой защищенной части приложения пользователю будет предложено
                // ввести имя пользователя и пароль и если они верны, то предоставит пользователю доступ
                .and()
                .csrf()//защита от атак межсайтовой подделки запросов
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())//позволяет получить доступ к токену CSRF через JavaScript.
                .ignoringRequestMatchers(
                        new AntPathRequestMatcher(this.adminServer.getContextPath()
                                + "/instances", HttpMethod.POST.toString()),
                        new AntPathRequestMatcher(this.adminServer.getContextPath()
                                + "/instances/*", HttpMethod.DELETE.toString()),
                        new AntPathRequestMatcher(this.adminServer.getContextPath() + "/actuator/**"))
                .and()
                .rememberMe()
                .key(UUID.randomUUID().toString())
                .tokenValiditySeconds(1209600);
        return http.build();
    }
}
