package com.example.demo.infra.httpclient.feature.header.provider;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.demo.infra.httpclient.feature.header.factory.HeaderValueResolverFactory;
import com.example.demo.infra.httpclient.feature.header.interceptor.CustomHeaderInterceptor;
import com.example.demo.infra.httpclient.core.RestClientInterceptorProvider;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 自訂 Header 攔截器提供者。
 *
 * <p>
 * 負責從 {@link ExternalSystemProperties} 取得系統專屬的 custom headers，並建立
 * {@link CustomHeaderInterceptor} 掛載到 {@link RestClient.Builder}。
 * </p>
 *
 * <p>
 * 職責：
 * <ul>
 * <li>取得系統設定的 custom headers</li>
 * <li>建立並設定 {@link CustomHeaderInterceptor}</li>
 * <li>將攔截器掛載至 {@link RestClient.Builder}</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@Order(15)
@RequiredArgsConstructor
public class CustomHeaderRestClientInterceptorProvider implements RestClientInterceptorProvider {

	private final ExternalSystemProperties properties;
	private final HeaderValueResolverFactory resolverFactory;

	@Override
	public boolean supports(String systemName) {
		return true;
	}

	@Override
	public void apply(RestClient.Builder builder, String systemName) {
		Map<String, String> customHeaders = properties.getCustomHeaders(systemName);

		if (customHeaders == null || customHeaders.isEmpty()) {
			return;
		}

		builder.requestInterceptor(new CustomHeaderInterceptor(systemName, customHeaders, resolverFactory));
	}
}
