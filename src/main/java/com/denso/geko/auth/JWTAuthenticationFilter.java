package com.denso.geko.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import com.denso.geko.Constants.CommonConstants;

/**
 * JWT認証フィルター
 *
 */
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    
    private AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder passwordEncoder;
    
    /**
     * Constructor
     * @param authenticationManager authenticationManager
     * @param bCryptPasswordEncoder PasswordEncoder
     */
    public JWTAuthenticationFilter(
            AuthenticationManager authenticationManager,
            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = bCryptPasswordEncoder;
        
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login", "POST"));
        
        setUsernameParameter(CommonConstants.LOGIN_ID);
        setPasswordParameter(CommonConstants.PASSWORD);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter#attemptAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {

        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getParameter(CommonConstants.LOGIN_ID),
                        request.getParameter(CommonConstants.PASSWORD),
                        new ArrayList<>()
                        )
                );
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter#successfulAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain, org.springframework.security.core.Authentication)
     */
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication auth
            ) throws IOException, ServletException {
        String token = Jwts.builder()
                .setSubject(((User)auth.getPrincipal()).getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + CommonConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, CommonConstants.SECRET.getBytes())
                .compact();
        response.addHeader(CommonConstants.HEADER_STRING, CommonConstants.TOKEN_PREFIX + token);
        
    }

}
