package com.example.demo.infra.httpclient.feature.error.provider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.demo.application.shared.exception.ExternalApiException;
import com.example.demo.infra.httpclient.feature.error.handler.ExternalApiErrorHandler;
import com.example.demo.infra.httpclient.feature.error.interceptor.ExternalApiExceptionInterceptor;
import com.example.demo.infra.httpclient.core.RestClientInterceptorProvider;
import com.example.demo.infra.httpclient.feature.error.registry.ExternalApiErrorHandlerRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP 異常攔截器提供者。
 *
 * <p>
 * 負責將 {@link ExternalApiExceptionInterceptor} 掛載到 {@link RestClient.Builder}。
 * </p>
 */
@Slf4j
@Component
@Order(30)
@RequiredArgsConstructor
public class ErrorInterceptorProvider implements RestClientInterceptorProvider {

	private final ExternalApiErrorHandlerRegistry registry;

	@Override
	public boolean supports(String systemName) {
		return true;
	}

	@Override
	public void apply(RestClient.Builder builder, String systemName) {
		builder.requestInterceptor(new ExternalApiExceptionInterceptor(systemName, registry));
	}
}
