package com.example.demo.infra.httpclient.core;

import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient Filter 提供者介面。
 *
 * <p>
 * 針對響應式 (Reactive) 的 {@link WebClient}，此介面負責將各系統專屬的 
 * {@link org.springframework.web.reactive.function.client.ExchangeFilterFunction} 
 * (例如：Token 認證、Logging、錯誤處理) 註冊到 {@link WebClient.Builder} 中。
 * </p>
 */
public interface WebClientFilterProvider {

	/**
	 * 判斷該 Provider 是否支援目標外部系統。
	 *
	 * @param systemName 外部系統名稱 (例如: auth, payment)
	 * @return 如果支援則回傳 true，反之則回傳 false
	 */
	boolean supports(String systemName);

	/**
	 * 將專屬的 ExchangeFilterFunction 套用至 WebClient.Builder 中。
	 *
	 * @param builder    正在建置中的 WebClient.Builder
	 * @param systemName 外部系統名稱
	 */
	void apply(WebClient.Builder builder, String systemName);
}