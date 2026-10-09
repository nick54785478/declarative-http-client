package com.example.demo.iface.handler;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.application.shared.exception.ExternalApiException;
import com.example.demo.iface.exception.GlobalErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 全域例外處理器。
 *
 * <p>
 * 統一處理系統內部拋出的例外，特別是 {@link ExternalApiException}。
 * 將外部系統錯誤轉換為統一對外格式，避免洩漏內部或外部系統細節。
 * </p>
 *
 * <p>
 * 設計目的：
 * <ul>
 * <li>記錄完整的錯誤日誌（包含 systemName、HTTP status、errorCode、traceId）</li>
 * <li>將外部系統錯誤轉換為統一對外回應 {@link GlobalErrorResponse}</li>
 * <li>統一回傳 HTTP 502 Bad Gateway，表示外部系統異常</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 處理 ExternalApiException。
	 *
	 * <p>
	 * 流程：
	 * <ol>
	 * <li>從 MDC 取得 traceId，用於追蹤整個分布式請求鏈</li>
	 * <li>記錄完整錯誤日誌</li>
	 * <li>建立統一對外回應 {@link GlobalErrorResponse}</li>
	 * <li>回傳 HTTP 502 Bad Gateway，表示外部系統異常</li>
	 * </ol>
	 * </p>
	 *
	 * @param ex 外部 API 異常
	 * @return 封裝後的統一錯誤回應，包含 systemName、traceId、錯誤訊息等欄位
	 */
	@ExceptionHandler(ExternalApiException.class)
	public ResponseEntity<GlobalErrorResponse> handleExternalApiException(ExternalApiException ex) {

		String traceId = MDC.get("traceId");

		log.error("[EXTERNAL-API-ERROR] system={} status={} code={} traceId={}", ex.getSystemName(), ex.getStatus(),
				ex.getErrorCode(), traceId, ex);

		GlobalErrorResponse response = GlobalErrorResponse.builder().code("EXTERNAL_API_ERROR").message(ex.getMessage()) // 注意：可能包含外部系統回應訊息
				.system(ex.getSystemName()).traceId(ex.getTraceId()).build();

		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
	}
}