package com.example.demo.infra.httpclient.feature.error.business;

/**
 * 業務錯誤判斷策略介面。
 *
 * <p>
 * 由各系統實作，決定 HTTP 2xx 時在何種情況算作業務錯誤，以及如何萃取錯誤訊息。
 * </p>
 */
public interface BusinessErrorStrategy {

	/**
	 * 是否支援該系統。
	 *
	 * @param systemName 系統名稱
	 * @return true: 支援, false: 不支援
	 */
	boolean supports(String systemName);

	/**
	 * 判斷是否為業務錯誤。
	 *
	 * @param responseBody HTTP 回應 body
	 * @return true: 為業務錯誤, false: 正常
	 */
	boolean isBusinessError(String responseBody);

	/**
	 * 從回應中萃取錯誤訊息。
	 *
	 * @param responseBody HTTP 回應 body
	 * @return 錯誤訊息字串
	 */
	String extractMessage(String responseBody);
}
