package com.example.demo.infra.httpclient.core;

import java.util.List;

import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebClientFactory
 *
 * <p>
 * 此工廠負責建立 {@link WebClient} 實體，並動態套用所有可用的響應式過濾器 (Reactive Filters)。
 * 每個外部系統可依據 {@code systemName} 取得對應配置。
 * </p>
 *
 * <p>
 * 功能：
 * <ul>
 * <li>建立 WebClient 並設定 Base URL</li>
 * <li>動態套用所有支援該系統的 {@link WebClientFilterProvider}</li>
 * <li>依照 {@code @Order} 註解排序 Provider，確保過濾器 (如：認證、日誌) 執行順序無誤</li>
 * </ul>
 * </p>
 *
 * <p>
 * 使用範例：
 * 
 * <pre>
 * WebClient client = webClientFactory.create("auth");
 * </pre>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientFactory {

	/**
	 * 外部系統設定檔對應實體
	 */
	private final ExternalSystemProperties properties;

	/**
	 * 所有的 WebClient 過濾器 Provider 集合
	 */
	private final List<WebClientFilterProvider> providers;

	/**
	 * 建立並初始化 WebClient 實體。
	 *
	 * <p>
	 * 流程：
	 * <ol>
	 * <li>取得系統的 Base URL</li>
	 * <li>建立 WebClient.Builder 實例</li>
	 * <li>依照 Order 排序，依序套用所有支援該系統的 Provider</li>
	 * <li>返回建置完成的 WebClient</li>
	 * </ol>
	 * </p>
	 *
	 * @param systemName 外部系統名稱 (如 "auth")
	 * @return 建置完成的 {@link WebClient}
	 */
	public WebClient create(String systemName) {
		String baseUrl = properties.getBaseUrl(systemName);
		log.info("建立 WebClient: system={} baseUrl={}", systemName, baseUrl);

		WebClient.Builder builder = WebClient.builder().baseUrl(baseUrl);

		// 套用所有支援該 system 的 Reactive Filter Provider
		providers.stream().sorted(OrderComparator.INSTANCE).filter(p -> p.supports(systemName))
				.forEach(p -> p.apply(builder, systemName));

		return builder.build();
	}
}