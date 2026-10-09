package com.example.demo.infra.httpclient.feature.retry.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.demo.infra.httpclient.feature.retry.interceptor.RetryInterceptor;
import com.example.demo.infra.httpclient.core.RestClientInterceptorProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * RetryInterceptor Provider。
 *
 * <p>
 * 將 {@link RetryInterceptor} 掛載到 {@link RestClient.Builder}，為外部系統提供失敗重試機制。
 * </p>
 *
 * <p>
 * 順序建議：
 * <ul>
 * <li>需在 Logging 之後（@Order(25)）</li>
 * <li>需在 Error / BusinessError 之前</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@Order(25)
public class RetryInterceptorProvider implements RestClientInterceptorProvider {

	/**
	 * 最大重試次數，預設 3次。
	 */
	@Value("${external.retry.max-attempts:3}")
	private int maxRetries;

	/**
	 * 每次重試間隔毫秒，預設 1000ms。
	 */
	@Value("${external.retry.delay-millis:1000}")
	private long retryDelayMillis;

	/**
	 * 判斷是否支援該系統。
	 *
	 * <p>
	 * 此 Provider 預設支援所有系統。
	 * </p>
	 *
	 * @param systemName 系統名稱
	 * @return true
	 */
	@Override
	public boolean supports(String systemName) {
		return true;
	}

	/**
	 * 將 RetryInterceptor 掛載到 RestClient Builder。
	 *
	 * @param builder    RestClient 建構器
	 * @param systemName 系統名稱
	 */
	@Override
	public void apply(RestClient.Builder builder, String systemName) {
		builder.requestInterceptor(new RetryInterceptor(systemName, maxRetries, retryDelayMillis));
	}
}
