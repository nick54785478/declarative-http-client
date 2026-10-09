package com.example.demo.infra.httpclient.feature.error.provider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.demo.infra.httpclient.feature.error.factory.BusinessErrorInterceptorFactory;
import com.example.demo.infra.httpclient.feature.error.interceptor.BusinessErrorInterceptor;
import com.example.demo.infra.httpclient.core.RestClientInterceptorProvider;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 業務錯誤攔截器提供者。
 *
 * <p>
 * 負責將 {@link BusinessErrorInterceptor} 掛載到 {@link RestClient.Builder}。
 * </p>
 */
@Slf4j
@Component
@Order(40)
@RequiredArgsConstructor
public class BusinessErrorInterceptorProvider implements RestClientInterceptorProvider {

	private final ExternalSystemProperties properties;
	private final BusinessErrorInterceptorFactory factory;

	@Override
	public boolean supports(String systemName) {
		ExternalSystemProperties.SystemConfig config = properties.getSystem(systemName);
		return config != null && Boolean.TRUE.equals(config.getEnableBusinessErrorHandling());
	}

	@Override
	public void apply(RestClient.Builder builder, String systemName) {
		builder.requestInterceptor(factory.create(systemName));
	}
}
