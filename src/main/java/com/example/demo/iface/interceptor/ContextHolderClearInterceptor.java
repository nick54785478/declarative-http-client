package com.example.demo.iface.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.demo.application.shared.context.ContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@code ContextHolderClearInterceptor} 負責在 Request 生命週期結束時， 清除
 * {@link ContextHolder} 中的 ThreadLocal 資料。
 *
 * <p>
 * 由於 Web Container（如 Tomcat）會重用執行緒處理不同的 HTTP Request， 若未在 Request 結束後明確清除
 * ThreadLocal，可能導致：
 * <ul>
 * <li>前一個 Request 的 Context 污染下一個 Request</li>
 * <li>使用者 / Tenant / TraceId 等資料錯置</li>
 * <li>僅在高併發或壓測時才出現的詭異問題</li>
 * </ul>
 * </p>
 *
 * <p>
 * 因此，本攔截器統一於
 * {@link #afterCompletion(HttpServletRequest, HttpServletResponse, Object, Exception)}
 * 階段執行清除邏輯，確保不論 Request 正常結束或發生 Exception， Context 都能被正確釋放。
 * </p>
 */
@Component
public class ContextHolderClearInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(ContextHolderClearInterceptor.class);

	/**
	 * Request 前置處理。
	 *
	 * <p>
	 * 本攔截器的核心責任在於「清除 Context」， 因此前置階段不進行任何初始化或修改行為， 僅保留 hook 位置以便未來擴充（例如 trace
	 * log）。
	 * </p>
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

		if (log.isDebugEnabled()) {
			log.debug("ContextHolder clear interceptor preHandle, uri={}", request.getRequestURI());
		}

		return true;
	}

	/**
	 * Request 完成後處理（finally 區段）。
	 *
	 * <p>
	 * 此方法會在以下所有情境被呼叫：
	 * <ul>
	 * <li>Controller 正常回傳</li>
	 * <li>Controller 拋出 Exception</li>
	 * <li>後續 Interceptor 或 Filter 發生錯誤</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * 因此這裡是清除 {@link ContextHolder} 的唯一且最安全位置。
	 * </p>
	 *
	 * @param ex 若 Request 處理過程中發生例外，則不為 null
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		try {
			ContextHolder.clear();

			if (log.isDebugEnabled()) {
				log.debug("ContextHolder cleared, uri={}, status={}", request.getRequestURI(), response.getStatus());
			}

		} catch (Exception clearEx) {
			// 清除失敗不應影響主流程，但必須留下可追蹤紀錄
			log.warn("Failed to clear ContextHolder, uri={}", request.getRequestURI(), clearEx);
		}
	}
}
