package com.denso.geko.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.denso.geko.service.LoginUserDetailsService;

@EnableWebSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
@ComponentScan
public class WebSecurityJtwConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private LoginUserDetailsService userDetailsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/api/**");
        http
            .authorizeRequests()
            .antMatchers("/api/login").permitAll()
            .anyRequest().authenticated()
            .and().logout()
            .and().cors().configurationSource(corsConfigurationSource()) // CORS　対応 '19.12.09
            .and().csrf().disable()
            .addFilter(new JWTAuthenticationFilter(authenticationManager(), passwordEncoder))
            .addFilter(new JWTAuthorizationFilter(authenticationManager()))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            ;
    }
    // CORS対応 '19.12.09
    @Autowired
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        corsConfiguration.setAllowCredentials(false);
        corsConfiguration.addExposedHeader("Content-Length, Authorization");
        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**", corsConfiguration);

        return corsSource;
    }
       
    @Autowired
    public void configureAuth(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
    }

}
