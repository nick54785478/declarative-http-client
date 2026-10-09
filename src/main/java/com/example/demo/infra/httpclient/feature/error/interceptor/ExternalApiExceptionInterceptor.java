package com.example.demo.infra.httpclient.feature.error.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.example.demo.application.shared.exception.ExternalApiException;
import com.example.demo.infra.httpclient.feature.error.handler.ExternalApiErrorHandler;
import com.example.demo.infra.httpclient.feature.error.registry.ExternalApiErrorHandlerRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 外部系統異常攔截器 (HTTP 4xx/5xx)。
 *
 * <p>
 * 攔截 HTTP 回應，若發生 HTTP 狀態碼異常 (4xx, 5xx)，則委派給對應的
 * {@link ExternalApiErrorHandlerRegistry} 解析並拋出統一例外。
 * </p>
 *
 * <p>
 * 流程：
 * <ol>
 * <li>取得 HTTP 請求回應 {@link ClientHttpResponse}</li>
 * <li>檢查狀態碼是否為錯誤 (4xx 或 5xx)
 * <ul>
 * <li>讀取回應 body</li>
 * <li>取得 traceId</li>
 * <li>向 {@link ExternalApiErrorHandlerRegistry} 查詢對應系統的
 * {@link ExternalApiErrorHandler}</li>
 * <li>拋出 {@link ExternalApiException} 統一例外</li>
 * </ul>
 * </li>
 * <li>若為 2xx 成功回應，則直接回傳</li>
 * </ol>
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class ExternalApiExceptionInterceptor implements ClientHttpRequestInterceptor {

	private final String systemName;
	private final ExternalApiErrorHandlerRegistry registry;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		ClientHttpResponse response = execution.execute(request, body);

		if (response.getStatusCode().isError()) {
			String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
			String traceId = request.getHeaders().getFirst("X-Trace-Id");

			log.info("[ExternalApiExceptionInterceptor] systemName={}, traceId={}", systemName, traceId);

			ExternalApiErrorHandler handler = registry.getHandler(systemName);

			throw handler.handle(systemName, traceId, response, responseBody);
		}

		return response;
	}
}
