package com.example.demo.application.shared.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 外部 API 呼叫異常封裝。
 *
 * <p>
 * 用於 RestClient 呼叫外部系統時，當 HTTP 回應狀態碼為 4xx 或 5xx，
 * 或者解析錯誤時拋出。統一封裝外部系統資訊，便於全域捕捉與日誌紀錄。
 * </p>
 *
 * <p>
 * 欄位說明：
 * <ul>
 * <li>systemName - 外部系統名稱</li>
 * <li>status - HTTP 狀態碼</li>
 * <li>errorCode - 外部系統錯誤代碼</li>
 * <li>responseBody - 原始回應內容</li>
 * <li>traceId - 追蹤 ID，可用於分布式追蹤與日誌關聯</li>
 * </ul>
 * </p>
 */
@Getter
@AllArgsConstructor
public class ExternalApiException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 外部系統名稱。
	 */
	private final String systemName;

	/**
	 * HTTP 狀態碼。
	 */
	private final int status;

	/**
	 * 外部系統錯誤代碼。
	 */
	private final String errorCode;

	/**
	 * 原始回應內容。
	 */
	private final String responseBody;

	/**
	 * 分布式追蹤 TraceId。
	 */
	private final String traceId;

	/**
	 * 建立 ExternalApiException。
	 *
	 * @param systemName   外部系統名稱
	 * @param status       HTTP 狀態碼
	 * @param errorCode    外部系統錯誤代碼
	 * @param message      錯誤訊息
	 * @param responseBody 原始回應內容
	 * @param traceId      分布式追蹤 TraceId
	 */
	public ExternalApiException(String systemName, int status, String errorCode, String message, String responseBody,
			String traceId) {
		super(message);
		this.systemName = systemName;
		this.status = status;
		this.errorCode = errorCode;
		this.responseBody = responseBody;
		this.traceId = traceId;
	}
}