package com.example.backend.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Logs all API request/response (method, URI, headers, body, status, duration) to file and console.
 * Only applies to /api/** and /s/** paths. Request/response body truncated for safety.
 */
@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);

    private static final int MAX_BODY_LOG_LENGTH = 2000;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api") && !path.startsWith("/s/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startMs = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long durationMs = System.currentTimeMillis() - startMs;

            String method = request.getMethod();
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            String fullUri = query != null ? uri + "?" + query : uri;
            int status = response.getStatus();

            String requestBody = getRequestBody(wrappedRequest, request.getRequestURI());
            String responseBody = getResponseBody(wrappedResponse);

            String message = String.format(
                    "[%s %s] status=%d time=%dms | request=%s | response=%s",
                    method, fullUri, status, durationMs,
                    truncate(maskSensitive(requestBody, uri)),
                    truncate(responseBody)
            );

            log.info(message);

            wrappedResponse.copyBodyToResponse();
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request, String uri) {
        byte[] buf = request.getContentAsByteArray();
        if (buf == null || buf.length == 0) return "";
        String s = new String(buf, StandardCharsets.UTF_8);
        return s;
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        if (buf == null || buf.length == 0) return "";
        return new String(buf, StandardCharsets.UTF_8);
    }

    private String maskSensitive(String body, String uri) {
        if (body == null || body.isEmpty()) return body;
        if (uri.contains("/api/auth/login") || uri.contains("/api/auth/signup")) {
            return body.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
        }
        return body;
    }

    private String truncate(String s) {
        if (s == null) return "";
        if (s.length() <= MAX_BODY_LOG_LENGTH) return s;
        return s.substring(0, MAX_BODY_LOG_LENGTH) + "... [truncated]";
    }
}
