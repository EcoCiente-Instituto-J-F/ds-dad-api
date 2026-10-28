package dad.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import dad.config.security.JwtAuthenticationFilter;
import dad.config.security.JwtService;
import dad.config.security.SecurityExceptionHandler;
import dad.config.security.UsuarioJwtService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtService jwtService,
      UsuarioJwtService usuarioService,
      SecurityExceptionHandler exceptionHandler,
      CorsConfigurationSource corsConfigurationSource) throws Exception {

    var jwtFilter = new JwtAuthenticationFilter(
        jwtService,
        usuarioService,
        exceptionHandler);

    return http
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .requestCache(AbstractHttpConfigurer::disable)

        .cors(cors -> cors.configurationSource(
            corsConfigurationSource))

        .sessionManagement(session -> session.sessionCreationPolicy(
            SessionCreationPolicy.STATELESS))

        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(exceptionHandler)
            .accessDeniedHandler(exceptionHandler))

        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                HttpMethod.OPTIONS,
                "/**")
            .permitAll()

            .requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/error")
            .permitAll()

            .requestMatchers(
                "/api/admin/v1/**")
            .hasRole("ADMIN")

            .anyRequest().denyAll())

        .addFilterBefore(
            jwtFilter,
            UsernamePasswordAuthenticationFilter.class)

        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.origens}") String origens) {
    var configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(
        Arrays.stream(origens.split(","))
            .map(String::trim)
            .filter(origem -> !origem.isEmpty())
            .toList());

    configuration.setAllowedMethods(
        List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"));

    configuration.setAllowedHeaders(
        List.of(
            "Authorization",
            "Content-Type",
            "Accept"));

    configuration.setExposedHeaders(
        List.of("Location"));

    configuration.setAllowCredentials(false);
    configuration.setMaxAge(3600L);

    var source = new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration(
        "/**",
        configuration);

    return source;
  }
}