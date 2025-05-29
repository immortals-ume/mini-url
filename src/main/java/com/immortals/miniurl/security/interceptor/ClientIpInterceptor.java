package com.immortals.miniurl.security.interceptor;

import com.immortals.miniurl.context.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.UnknownHostException;

import static com.immortals.miniurl.constants.UrlConstants.MDC_USER_AGENT_KEY;

@Component
@Slf4j
public class ClientIpInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws UnknownHostException {


        String userAgent = extractUserAgent(request);

        log.debug("Incoming request from IP: {}, User-Agent: {}", userAgent);

        MDC.put(MDC_USER_AGENT_KEY, userAgent);

        request.setAttribute(MDC_USER_AGENT_KEY, userAgent);

        RequestContext.setUserAgent(userAgent);

        return Boolean.TRUE;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(MDC_USER_AGENT_KEY);
    }

    private String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return (userAgent != null && !userAgent.isEmpty()) ? userAgent : "unknown";
    }
}
