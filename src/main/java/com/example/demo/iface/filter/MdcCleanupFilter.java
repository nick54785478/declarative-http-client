package com.example.demo.iface.filter;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.infra.httpclient.feature.tracing.interceptor.TracingInterceptor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * MDC 清除過濾器。
 *
 * <p>
 * 用於 Servlet 請求過濾器中，在請求結束後清除 MDC（Mapped Diagnostic Context），
 * 避免執行緒池重複使用導致下一次請求殘留上一次請求的日誌追蹤資訊。
 * </p>
 *
 * <p>
 * 功能：
 * <ul>
 * <li>請求進入時允許正常執行 filterChain</li>
 * <li>請求結束後（無論是否發生異常），清除 MDC</li>
 * <li>確保每個請求的 MDC 都是乾淨的</li>
 * </ul>
 * </p>
 *
 * <p>
 * 適用於搭配 {@link TracingInterceptor} 或任何寫入 MDC 的日誌追蹤方案。
 * </p>
 */
@Component
public class MdcCleanupFilter extends OncePerRequestFilter {

	/**
	 * 執行請求過濾，並於結束時清除 MDC。
	 *
	 * @param request     HTTP 請求
	 * @param response    HTTP 回應
	 * @param filterChain 過濾鏈
	 * @throws ServletException 過濾器執行中拋出
	 * @throws IOException      過濾器執行中拋出
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			filterChain.doFilter(request, response);
		} finally {
			// 清除 MDC，避免執行緒共用導致上下文污染
			MDC.clear();
		}
	}
}