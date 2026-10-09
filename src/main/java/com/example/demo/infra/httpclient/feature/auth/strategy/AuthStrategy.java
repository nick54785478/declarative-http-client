package com.example.demo.infra.httpclient.feature.auth.strategy;

import org.springframework.http.HttpRequest;

import com.example.demo.infra.shared.properties.ExternalSystemProperties;

/**
 * 認證策略介面。
 *
 * <p>
 * 不同的認證策略（JWT / Basic / None）實作此介面，負責在 HTTP Request 上加上對應的 Authorization
 * Header。
 * </p>
 */
public interface AuthStrategy {

	/**
	 * 是否支援此 authType。
	 *
	 * @param authType 認證類型
	 * @return true: 支援, false: 不支援
	 */
	boolean supports(String authType);

	/**
	 * 套用認證資訊至 HTTP Request。
	 *
	 * @param request HTTP Request
	 * @param config  系統設定
	 */
	void apply(HttpRequest request, ExternalSystemProperties.SystemConfig config);
}
