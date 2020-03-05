package com.denso.geko.auth;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.denso.geko.Constants.CommonConstants;

import io.jsonwebtoken.Jwts;

/**
 * JWT認可フィルター
 *
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter  {
    
    
    /**
     * Constructor
     * @param authenticationManager authenticationManager
     */
    public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.www.BasicAuthenticationFilter#doFilterInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
            ) throws IOException, ServletException {
        // リクエストヘッダーより、認証キー(JWT)を取得する
        String header = request.getHeader(CommonConstants.HEADER_STRING);
        
        if (header == null || !header.startsWith(CommonConstants.TOKEN_PREFIX)) {
            chain.doFilter(request,  response);
            return;
        }
        
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request,  response);
    }
    
    /**
     * リクエストよりJWTを取得し、パースしたユーザ情報を返す
     * @param request リクエスト
     * @return ユーザ情報
     */
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(CommonConstants.HEADER_STRING);
        if (token != null) {
            String user = Jwts.parser()
                    .setSigningKey(CommonConstants.SECRET.getBytes())
                    .parseClaimsJws(token.replaceAll(CommonConstants.TOKEN_PREFIX,  ""))
                    .getBody()
                    .getSubject();
            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }
        }
        return null;
    }

}
