package com.example.demo.infra.httpclient.core;

import org.springframework.web.client.RestClient;

/**
 * RestClient 攔截器提供者介面。
 *
 * <p>
 * 每個 Provider 可以決定自己是否支援該 systemName， 並將專屬的攔截器掛載到 RestClient.Builder。
 * </p>
 */
public interface RestClientInterceptorProvider {

	/**
	 * 判斷該 Provider 是否適用於指定 systemName。
	 *
	 * @param systemName 系統名稱
	 * @return true: 支援, false: 不支援
	 */
	boolean supports(String systemName);

	/**
	 * 將攔截器套用至 RestClient.Builder。
	 *
	 * @param builder    RestClient 建立器
	 * @param systemName 系統名稱
	 */
	void apply(RestClient.Builder builder, String systemName);
}
