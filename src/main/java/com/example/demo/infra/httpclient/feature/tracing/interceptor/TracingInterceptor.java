package com.example.demo.infra.httpclient.feature.tracing.interceptor;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP Trace 攔截器。
 *
 * <p>
 * 負責為每個 HTTP 請求建立或延用 TraceId，用於日誌追蹤：
 * <ul>
 * <li>檢查是否已有 X-Trace-Id，若無則產生新的 UUID</li>
 * <li>將 TraceId 寫入 HTTP Header（X-Trace-Id）</li>
 * <li>將 TraceId 放入 MDC（Mapped Diagnostic Context）， 使 log pattern 可印出</li>
 * </ul>
 * </p>
 *
 * <p>
 * 注意：
 * <ul>
 * <li>攔截器僅負責 TraceId 寫入與 MDC 設定，不處理其他業務邏輯或例外轉換</li>
 * <li>建議攔截器順序排在最前（@Order(5)），以便後續攔截器使用相同 traceId</li>
 * </ul>
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class TracingInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * HTTP Trace Header 名稱。
	 */
	private static final String TRACE_HEADER = "X-Trace-Id";

	/**
	 * MDC Key 名稱。
	 */
	private static final String MDC_KEY = "traceId";

	/**
	 * 系統名稱，用於日誌追蹤。
	 */
	private final String systemName;

	/**
	 * 攔截 HTTP 請求，產生或延用 TraceId。
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

		// 若已存在 TraceId（例如從外層傳入），則沿用
		String traceId = request.getHeaders().getFirst(TRACE_HEADER);

		if (traceId == null) {
			traceId = UUID.randomUUID().toString();
			request.getHeaders().add(TRACE_HEADER, traceId);
		}

		// 放入 MDC，讓 log pattern 可以印出
		MDC.put(MDC_KEY, traceId);

		log.debug("[TRACE] system={} traceId={}", systemName, traceId);
		return execution.execute(request, body);
	}
}
