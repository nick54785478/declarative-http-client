package com.example.demo.infra.httpclient.feature.tracing.provider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.demo.infra.httpclient.feature.tracing.interceptor.TracingInterceptor;
import com.example.demo.infra.httpclient.core.RestClientInterceptorProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * TracingInterceptor Provider。
 *
 * <p>
 * 將 {@link TracingInterceptor} 掛載到 {@link RestClient.Builder}，確保外部系統請求皆有統一
 * TraceId 寫入及 MDC 記錄。
 * </p>
 *
 * <p>
 * 職責：
 * <ul>
 * <li>為每個 HTTP 請求確保有統一 TraceId</li>
 * <li>將 TraceId 放入 MDC 供日誌追蹤</li>
 * <li>建議在所有攔截器之前執行，確保後續攔截器皆能取得同一個 traceId</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@Order(5)
public class TracingInterceptorProvider implements RestClientInterceptorProvider {

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
	 * 將 TracingInterceptor 掛載到 RestClient Builder。
	 *
	 * @param builder    RestClient 建構器
	 * @param systemName 系統名稱
	 */
	@Override
	public void apply(RestClient.Builder builder, String systemName) {
		builder.requestInterceptor(new TracingInterceptor(systemName));
	}
}
