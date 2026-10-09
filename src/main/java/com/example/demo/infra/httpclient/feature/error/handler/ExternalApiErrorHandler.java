package com.example.demo.infra.httpclient.feature.error.handler;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;

import com.example.demo.application.shared.exception.ExternalApiException;

/**
 * 外部 API 錯誤處理器介面。
 *
 * <p>
 * 定義外部系統 HTTP 錯誤 (4xx, 5xx) 的處理規範。各個系統可實作專屬的錯誤解析邏輯。
 * </p>
 *
 * <p>
 * 職責：
 * <ul>
 * <li>解析特定外部系統的錯誤回應格式</li>
 * <li>將外部錯誤轉換為系統統一的 {@link ExternalApiException}</li>
 * </ul>
 * </p>
 */
public interface ExternalApiErrorHandler {

	/**
	 * 是否支援處理該系統的錯誤。
	 *
	 * @param systemName 系統名稱
	 * @return true: 支援, false: 不支援
	 */
	boolean supports(String systemName);

	/**
	 * 處理 HTTP 錯誤回應。
	 *
	 * @param systemName   系統名稱
	 * @param traceId      追蹤 ID
	 * @param response     HTTP 回應物件
	 * @param responseBody HTTP 回應內容字串
	 * @return 拋出或回傳的 {@link ExternalApiException}
	 * @throws IOException IO 例外
	 */
	ExternalApiException handle(
		    String systemName,
		    String traceId,
		    ClientHttpResponse response,
		    String responseBody
		) throws IOException;
}
