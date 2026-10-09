package com.example.demo.infra.httpclient.feature.error.handler.impl;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.example.demo.application.shared.exception.ExternalApiException;
import com.example.demo.infra.httpclient.feature.error.handler.ExternalApiErrorHandler;
import com.example.demo.infra.httpclient.feature.error.res.impl.AuthExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * AuthErrorHandler
 *
 * <p>
 * auth 外部系統專屬錯誤處理器。
 * </p>
 *
 * <p>
 * 職責：
 * <ul>
 * <li>處理 auth 系統的錯誤回應</li>
 * <li>將錯誤 JSON 轉換為 {@link AuthExceptionResponse}</li>
 * <li>封裝為統一的 {@link ExternalApiException}</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class AuthErrorHandler implements ExternalApiErrorHandler {

    private final ObjectMapper objectMapper;

    public AuthErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String systemName) {
        return "auth".equalsIgnoreCase(systemName);
    }

    @Override
    public ExternalApiException handle(String systemName, String traceId, ClientHttpResponse response,
            String responseBody) throws IOException {
        
        log.error("[AuthErrorHandler] systemName={}, status={}, body={}", systemName, response.getStatusCode(), responseBody);
        
        try {
            AuthExceptionResponse errorRes = objectMapper.readValue(responseBody, AuthExceptionResponse.class);
            return new ExternalApiException(systemName, response.getStatusCode().value(), errorRes.getCode(),
                    errorRes.getMessage(), traceId);
        } catch (Exception e) {
            return new ExternalApiException(systemName, response.getStatusCode().value(), "AUTH_SYSTEM_ERROR",
                    responseBody, traceId);
        }
    }
}
