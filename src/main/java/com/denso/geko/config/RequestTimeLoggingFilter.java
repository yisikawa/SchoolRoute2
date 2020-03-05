package com.denso.geko.config;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

public class RequestTimeLoggingFilter extends AbstractRequestLoggingFilter {
    
    Log log = LogFactory.getLog(RequestTimeLoggingFilter.class);

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        // css,jsはログ出力対象外
        if (request.getRequestURI().matches("^/(css|js)/.*")) {
            return false;
        }
        return log.isInfoEnabled();
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        log.info(message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        log.info(message);
    }
}