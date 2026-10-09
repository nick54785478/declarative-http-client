package com.example.demo.infra.httpclient.feature.logging.interceptor;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP 日誌攔截器。
 *
 * <p>
 * 負責攔截 HTTP 請求並記錄請求與回應資訊。
 * </p>
 *
 * <p>
 * 流程：
 * <ol>
 * <li>取得 HTTP 請求，記錄 Method, URL 等資訊</li>
 * <li>執行請求，計算花費時間</li>
 * <li>記錄回應狀態碼及花費時間</li>
 * <li>若發生例外，記錄錯誤日誌</li>
 * </ol>
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

	private static final String TRACE_HEADER = "X-Trace-Id";
	private static final Set<String> SENSITIVE_HEADERS = Set.of(HttpHeaders.AUTHORIZATION, "X-Api-Key");

	/**
	 * 系統名稱，用於日誌追蹤。
	 */
	private final String systemName;

	/**
	 * 攔截 HTTP 請求並記錄請求與回應資訊。
	 *
	 * @param request   HTTP Request
	 * @param body      Request Body
	 * @param execution 執行器
	 * @return ClientHttpResponse
	 * @throws IOException 請求時發生 IO 例外
	 */
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		long start = System.currentTimeMillis();
		String traceId = request.getHeaders().getFirst(TRACE_HEADER);

		String headerLog = formatHeaders(request.getHeaders());

		log.info("[HTTP-REQUEST] system={} traceId={} method={} url={} headers={}", systemName, traceId,
				request.getMethod(), request.getURI(), headerLog);

		try {

			ClientHttpResponse response = execution.execute(request, body);

			long cost = System.currentTimeMillis() - start;

			log.info("[HTTP-RESPONSE] system={} traceId={} method={} url={} status={} cost={}ms", systemName, traceId,
					request.getMethod(), request.getURI(), response.getStatusCode(), cost);

			return response;

		} catch (IOException | RuntimeException ex) {

			long cost = System.currentTimeMillis() - start;

			log.error("[HTTP-ERROR] system={} traceId={} method={} url={} cost={}ms message={}", systemName, traceId,
					request.getMethod(), request.getURI(), cost, ex.getMessage(), ex);

			throw ex;
		}
	}

	/**
	 * 將 Header 格式化為字串。
	 *
	 * <p>
	 * 若為敏感 Header（如 Authorization, X-Api-Key），則遮蔽為 "***"
	 * </p>
	 *
	 * @param headers HTTP Headers
	 * @return 格式化後字串
	 */
	private String formatHeaders(HttpHeaders headers) {

		StringBuilder sb = new StringBuilder();

		headers.forEach((key, values) -> {

			if (isSensitive(key)) {
				sb.append(key).append("=***, ");
			} else {
				sb.append(key).append("=").append(String.join(",", values)).append(", ");
			}
		});

		if (sb.length() > 2) {
			sb.setLength(sb.length() - 2);
		}

		return sb.toString();
	}

	/**
	 * 判斷 Header 是否為敏感資訊。
	 *
	 * @param headerName Header 名稱
	 * @return true: 為敏感, false: 非敏感
	 */
	private boolean isSensitive(String headerName) {
		return SENSITIVE_HEADERS.stream().anyMatch(s -> s.equalsIgnoreCase(headerName));
	}
}
