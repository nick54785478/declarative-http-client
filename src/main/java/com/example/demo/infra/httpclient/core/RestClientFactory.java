package com.example.demo.infra.httpclient.core;

import java.util.List;

import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.demo.infra.httpclient.core.RestClientInterceptorProvider;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RestClientFactory
 *
 * <p>
 * 此工廠負責建立 RestClient 實體，並動態套用所有可用的攔截器 Provider。每個外部系統可依據 systemName 取得對應配置。
 * </p>
 *
 * <p>
 * 功能：
 * <ul>
 * <li>建立 RestClient 並設定 Base URL</li>
 * <li>動態套用所有支援該 system 的 RestClientInterceptorProvider</li>
 * <li>依照 @Order 註解排序 Provider，確保攔截器執行順序無誤</li>
 * </ul>
 * </p>
 *
 * <p>
 * 使用範例：
 * 
 * <pre>
 * RestClient client = restClientFactory.create("sqms8d");
 * </pre>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestClientFactory {

	/**
	 * 外部系統設定
	 */
	private final ExternalSystemProperties properties;

	/**
	 * 所有的 RestClient 攔截器 Provider
	 */
	private final List<RestClientInterceptorProvider> providers;

	/**
	 * 建立 RestClient 實體。
	 *
	 * <p>
	 * 流程：
	 * <ol>
	 * <li>取得系統 Base URL</li>
	 * <li>建立 RestClient.Builder</li>
	 * <li>依照 Order 排序，依序套用所有支援的 Provider</li>
	 * <li>返回建置完成的 RestClient</li>
	 * </ol>
	 * </p>
	 *
	 * @param systemName 外部系統名稱
	 * @return 建置完成的 RestClient
	 */
	public RestClient create(String systemName) {

		String baseUrl = properties.getBaseUrl(systemName);

		log.info("建立 RestClient: system={} baseUrl={}", systemName, baseUrl);

		RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);

		// 套用所有支援該 system 的 Provider
		providers.stream().sorted(OrderComparator.INSTANCE).filter(p -> p.supports(systemName))
				.forEach(p -> p.apply(builder, systemName));

		return builder.build();
	}
}
