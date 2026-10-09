package com.example.demo.infra.httpclient.feature.auth.provider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.demo.infra.httpclient.feature.auth.factory.AuthStrategyFactory;
import com.example.demo.infra.httpclient.feature.auth.strategy.AuthStrategy;
import com.example.demo.infra.httpclient.feature.auth.interceptor.AuthenticationInterceptor;
import com.example.demo.infra.httpclient.core.RestClientInterceptorProvider;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 認證攔截器提供者 (Provider)。
 *
 * <p>
 * 此 Provider 負責讀取 {@link ExternalSystemProperties} 的設定，為指定的 {@code systemName}
 * 建立對應的 {@link AuthenticationInterceptor}。
 * </p>
 *
 * <p>
 * 職責：
 * <ul>
 * <li>判斷該系統是否需要套用認證攔截器</li>
 * <li>根據 {@code authType} 取得對應的 {@link AuthStrategy}</li>
 * <li>建立 {@link AuthenticationInterceptor} 並掛載到 {@link RestClient.Builder}</li>
 * </ul>
 * </p>
 *
 * <p>
 * 注意：
 * <ul>
 * <li>本類別僅負責掛載攔截器，並不包含實際的 HTTP 處理邏輯</li>
 * <li>攔截器的實際執行細節請參考 {@link AuthenticationInterceptor} 類別</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class AuthenticationInterceptorProvider implements RestClientInterceptorProvider {

	/**
	 * 外部系統設定，用於判斷系統是否有設定認證屬性。
	 */
	private final ExternalSystemProperties properties;

	/**
	 * 認證策略工廠，根據 {@code authType} 取得對應的 {@link AuthStrategy}。
	 */
	private final AuthStrategyFactory authStrategyFactory;

	/**
	 * 判斷該系統是否支援認證攔截器。
	 *
	 * @param systemName 系統名稱
	 * @return {@code true} 若系統設定中包含 {@code authType}，否則 {@code false}
	 */
	@Override
	public boolean supports(String systemName) {
		ExternalSystemProperties.SystemConfig config = properties.getSystem(systemName);

		return config != null && config.getAuthType() != null;
	}

	/**
	 * 將認證攔截器掛載到 {@link RestClient.Builder}。
	 *
	 * <p>
	 * 流程：
	 * <ol>
	 * <li>取得該系統設定的 {@link ExternalSystemProperties.SystemConfig}</li>
	 * <li>透過 {@link AuthStrategyFactory} 取得對應的 {@link AuthStrategy}</li>
	 * <li>建立 {@link AuthenticationInterceptor} 並加入 Builder</li>
	 * </ol>
	 * </p>
	 *
	 * @param builder    RestClient 建構器
	 * @param systemName 系統名稱
	 */
	@Override
	public void apply(RestClient.Builder builder, String systemName) {
		ExternalSystemProperties.SystemConfig config = properties.getSystem(systemName);

		AuthStrategy strategy = authStrategyFactory.getStrategy(config.getAuthType());

		builder.requestInterceptor(new AuthenticationInterceptor(systemName, config, strategy));
	}
}
