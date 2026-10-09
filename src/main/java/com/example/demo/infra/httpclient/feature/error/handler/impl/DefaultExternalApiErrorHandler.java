package com.example.demo.infra.httpclient.feature.error.handler.impl;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.example.demo.application.shared.exception.ExternalApiException;
import com.example.demo.infra.httpclient.feature.error.handler.ExternalApiErrorHandler;
import com.example.demo.infra.httpclient.feature.error.res.impl.Sqm8dExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * DefaultExternalApiErrorHandler
 *
 * <p>
 * 預設的外部系統錯誤處理器。
 * </p>
 *
 * <p>
 * 職責：
 * <ul>
 * <li>處理沒有專屬 Handler 的系統錯誤</li>
 * <li>封裝為統一的 {@link ExternalApiException}</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class DefaultExternalApiErrorHandler implements ExternalApiErrorHandler {

    @Override
    public boolean supports(String systemName) {
        return false;
    }

    @Override
    public ExternalApiException handle(String systemName, String traceId, ClientHttpResponse response,
            String responseBody) throws IOException {
        
        log.error("[DefaultExternalApiErrorHandler] systemName={}, status={}, body={}", systemName, response.getStatusCode(), responseBody);
        
        return new ExternalApiException(systemName, response.getStatusCode().value(), "EXTERNAL_SYSTEM_ERROR",
                responseBody, traceId);
    }
}
