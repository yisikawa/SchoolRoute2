package com.denso.geko.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AppConfig {
 
    @Value("${server.tomcat.connector.scheme}")
    private String tomcatScheme;

    @Value("${server.tomcat.connector.proxyname}")
    private String tomcatProxyName;

    @Value("${server.tomcat.connector.proxyport}")
    private int tomcatProxyPort;

    @Value("${server.tomcat.connector.secure}")
    private boolean tomcatSecure;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public RequestTimeLoggingFilter requestLoggingFilter() {
        RequestTimeLoggingFilter filter = new RequestTimeLoggingFilter();
        // ログ出力項目の設定...
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(false);
        filter.setIncludePayload(false);
        return filter;
    }
    
    @Bean
    public TomcatServletWebServerFactory containerFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);
                connector.setScheme(tomcatScheme);
                connector.setProxyName(tomcatProxyName);
                connector.setProxyPort(tomcatProxyPort);
                connector.setSecure(tomcatSecure);
            }
        };
    }
}