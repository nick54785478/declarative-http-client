package com.example.demo.infra.httpclient.feature.auth.interceptor;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.example.demo.infra.httpclient.feature.auth.strategy.AuthStrategy;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 外部系統認證攔截器。
 *
 * <p>
 * 此攔截器負責在 {@link org.springframework.web.client.RestTemplate} 或其他 Spring HTTP
 * 客戶端發送請求前處理認證：
 * <ul>
 * <li>移除舊的 {@code Authorization} Header，避免殘留錯誤資訊</li>
 * <li>透過 {@link AuthStrategy} 套用新的認證資訊（例如 Bearer Token 或 Basic Auth）</li>
 * <li>將部分認證資訊（已遮蔽）記錄於日誌，用於偵錯或監控</li>
 * </ul>
 * </p>
 *
 * <p>
 * 注意：
 * <ul>
 * <li>此類僅負責單一外部系統的認證邏輯</li>
 * <li>是否套用攔截由外部 Provider 控制，不在此類範圍</li>
 * </ul>
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * 系統名稱，用於日誌追蹤。
	 */
	private final String systemName;

	/**
	 * 外部系統設定，包含認證類型、憑證資訊等。
	 */
	private final ExternalSystemProperties.SystemConfig config;

	/**
	 * 認證策略，用於根據系統設定套用對應的 Authorization Header。
	 */
	private final AuthStrategy strategy;

	/**
	 * 攔截 HTTP 請求並套用認證策略。
	 *
	 * <p>
	 * 流程：
	 * <ol>
	 * <li>移除舊的 {@code Authorization} Header</li>
	 * <li>呼叫 {@link AuthStrategy#apply(HttpRequest, ExternalSystemProperties.SystemConfig)} 套用認證策略</li>
	 * <li>將遮蔽後的 Header 記錄至日誌</li>
	 * </ol>
	 * </p>
	 *
	 * @param request   HTTP 請求
	 * @param body      請求 Body
	 * @param execution 請求執行器
	 * @return HTTP 回應
	 * @throws IOException 若請求發送發生 IO 錯誤
	 */
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		// 移除 Authorization，避免殘留舊的 Token
		request.getHeaders().remove(HttpHeaders.AUTHORIZATION);

		// 套用策略
		strategy.apply(request, config);

		// 記錄日誌（將 Token 遮蔽後列印）
		log.info("[AUTH] system={} type={} header={}", systemName, config.getAuthType(),
				mask(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)));

		// 繼續執行請求
		return execution.execute(request, body);
	}

	/**
	 * 遮蔽敏感字串。
	 *
	 * <p>
	 * 將字串前 10 個字元保留，其餘替換為 "..." 以避免日誌外洩敏感 Token。若長度不足則不遮蔽。
	 * </p>
	 *
	 * @param value 原始字串
	 * @return 遮蔽後字串
	 */
	private String mask(String value) {
		if (value == null) {
			return null;
		}
		return value.length() > 10 ? value.substring(0, 10) + "..." : value;
	}
}