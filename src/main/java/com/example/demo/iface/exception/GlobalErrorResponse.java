package com.example.demo.iface.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 對外統一錯誤回應格式。
 */
@Data
@Builder
@AllArgsConstructor
public class GlobalErrorResponse {

	/**
	 * 系統內部錯誤碼
	 */
	private String code;

	/**
	 * 錯誤訊息
	 */
	private String message;

	/**
	 * 發生錯誤的外部系統
	 */
	private String system;

	/**
	 * 追蹤 ID (如果有寫 log 可使用供追蹤)
	 */
	private String traceId;
}