package com.denso.geko.auth;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.denso.geko.service.LoginUserDetailsService;

@EnableWebSecurity
@ComponentScan
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private LoginUserDetailsService userDetailsService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/css/**", "/js/**", "/img/**", "/login", "/favicon.ico", "/api/**").permitAll()
            .antMatchers("/map").hasRole("USER")
            .antMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and().formLogin()
                .loginPage("/login")
                    .usernameParameter("userid")
                    .passwordParameter("password")
                .loginProcessingUrl("/web/login")
                .defaultSuccessUrl("/map")
                .failureUrl("/login?error").permitAll()
            .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true).permitAll()
            .and().rememberMe()
                // TODO リリース時はコメントアウトを解除（SSL以外はCookieを送信しなくなる)
//                .useSecureCookie(true)
                .tokenValiditySeconds(1209600) // remember for 2 week. ( 60sec * 60min * 24h * 14days ) sec
                .tokenRepository(createTokenRepository())
            .and().csrf().disable()     // AWSでは正常動作しないためCSRFは無効とする
            ;
    }
    
    @Autowired
    void configureAuthenticationManager(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder)
            ;
    }
    
    /**
    * Remember Me 認証に利用するトークンのリポジトリ
   * @return 登録済みのユーザ情報
   */
   public PersistentTokenRepository createTokenRepository() {
       JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
       repository.setDataSource(dataSource);
       return repository;
   }
   
}
