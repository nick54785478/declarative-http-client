package com.example.demo.infra.httpclient.feature.error.registry;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.error.handler.ExternalApiErrorHandler;

/**
 * 外部 API 錯誤處理器註冊表 (External API Error Handler Registry)。
 *
 * <p>
 * 集中管理所有的 {@link ExternalApiErrorHandler} 實作。根據系統名稱 (System Name) 提供專屬的
 * 4xx/5xx 錯誤處理器。
 * </p>
 *
 * <p>
 * 設計模式 (Strategy & Registry Patterns)：
 * <ul>
 * <li>將不同外部系統的錯誤解析邏輯，抽離至獨立的處理器 (Handler) 中維護</li>
 * <li>透過 systemName 動態選擇錯誤處理器，避免大量的 if-else 或 switch 判斷</li>
 * <li>提供預設處理器 (Fallback)：若找不到對應處理器，則使用預設處理器 (Default Handler)</li>
 * </ul>
 * </p>
 *
 * @see ExternalApiErrorHandler
 */
@Component
public class ExternalApiErrorHandlerRegistry {

	private final List<ExternalApiErrorHandler> handlers;
	private final ExternalApiErrorHandler defaultHandler;

	public ExternalApiErrorHandlerRegistry(List<ExternalApiErrorHandler> handlers,
			ExternalApiErrorHandler defaultHandler) {
		this.handlers = handlers;
		this.defaultHandler = defaultHandler;
	}

	public ExternalApiErrorHandler getHandler(String systemName) {
		for (ExternalApiErrorHandler handler : handlers) {
			if (handler.supports(systemName)) {
				return handler;
			}
		}
		return defaultHandler;
	}
}
