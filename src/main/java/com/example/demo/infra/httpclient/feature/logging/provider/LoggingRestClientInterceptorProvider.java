package com.example.demo.infra.httpclient.feature.logging.provider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.demo.infra.httpclient.feature.logging.interceptor.LoggingInterceptor;
import com.example.demo.infra.httpclient.core.RestClientInterceptorProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * LoggingInterceptor Provider。
 *
 * <p>
 * 負責為系統設定 {@link LoggingInterceptor}，以列印統一的 HTTP 請求與回應日誌資訊。
 * </p>
 *
 * <p>
 * 職責：
 * <ul>
 * <li>對所有系統皆回傳 supports 為 true</li>
 * <li>將 LoggingInterceptor 掛載到 {@link RestClient.Builder}</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@Order(20)
public class LoggingRestClientInterceptorProvider implements RestClientInterceptorProvider {

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
	 * 將 LoggingInterceptor 掛載到 RestClient Builder。
	 *
	 * @param builder    RestClient 建構器
	 * @param systemName 系統名稱
	 */
	@Override
	public void apply(RestClient.Builder builder, String systemName) {
		builder.requestInterceptor(new LoggingInterceptor(systemName));
	}
}
