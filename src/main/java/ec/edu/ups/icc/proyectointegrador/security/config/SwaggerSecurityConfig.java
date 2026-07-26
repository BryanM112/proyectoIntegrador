package ec.edu.ups.icc.proyectointegrador.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
public class SwaggerSecurityConfig {

    @Value("${swagger.username}")
    private String swaggerUsername;

    @Value("${swagger.password}")
    private String swaggerPassword;

    @Bean
    public InMemoryUserDetailsManager swaggerUserDetailsService(
            PasswordEncoder passwordEncoder
    ) {
        UserDetails swaggerUser = User.builder()
                .username(swaggerUsername)
                .password(passwordEncoder.encode(swaggerPassword))
                .roles("SWAGGER")
                .build();

        return new InMemoryUserDetailsManager(swaggerUser);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain swaggerFilterChain(
            HttpSecurity http,
            InMemoryUserDetailsManager swaggerUserDetailsService,
            PasswordEncoder passwordEncoder
    ) throws Exception {

        AuthenticationManagerBuilder authManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authManagerBuilder
                .userDetailsService(swaggerUserDetailsService)
                .passwordEncoder(passwordEncoder);

        AuthenticationManager swaggerAuthManager =
                authManagerBuilder.build();

        BasicAuthenticationEntryPoint entryPoint =
                new BasicAuthenticationEntryPoint();

        entryPoint.setRealmName("Swagger");
        entryPoint.afterPropertiesSet();

        return http
                .securityMatcher(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs",
                        "/v3/api-docs/**"
                )

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authenticationManager(swaggerAuthManager)

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasRole("SWAGGER")
                )

                .anonymous(anonymous -> anonymous.disable())

                .httpBasic(basic -> basic
                        .authenticationEntryPoint(entryPoint)
                )

                .build();
    }
}