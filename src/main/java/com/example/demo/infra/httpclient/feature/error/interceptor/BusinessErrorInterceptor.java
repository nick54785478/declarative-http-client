package com.example.demo.infra.httpclient.feature.error.interceptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.example.demo.application.shared.exception.ExternalApiException;
import com.example.demo.infra.httpclient.feature.error.business.BusinessErrorStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 業務錯誤攔截器。
 *
 * <p>
 * 攔截 HTTP 回應，檢查是否屬於業務錯誤 (HTTP 2xx 但內容為失敗)。
 * 透過委派給 {@link BusinessErrorStrategy} 進行判斷。
 * </p>
 *
 * <p>
 * 流程：
 * <ol>
 * <li>執行請求並取得回應 {@link ClientHttpResponse}</li>
 * <li>若 HTTP 狀態碼為 2xx，則準備讀取內容</li>
 * <li>讀取回應 Body，並委派 {@link BusinessErrorStrategy#isBusinessError(String)}
 * 判斷是否為業務錯誤</li>
 * <li>若為業務錯誤，取得 Header 中的 traceId 並拋出 {@link ExternalApiException}</li>
 * <li>若非業務錯誤，將 Body 重新包裝並回傳</li>
 * </ol>
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class BusinessErrorInterceptor implements ClientHttpRequestInterceptor {

	private final String systemName;
	private final BusinessErrorStrategy strategy;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		ClientHttpResponse response = execution.execute(request, body);

		if (!response.getStatusCode().is2xxSuccessful()) {
			return response;
		}

		byte[] responseBytes = response.getBody().readAllBytes();
		String responseBody = new String(responseBytes, StandardCharsets.UTF_8);

		if (strategy.isBusinessError(responseBody)) {
			String traceId = request.getHeaders().getFirst("X-Trace-Id");

			throw new ExternalApiException(systemName, HttpStatus.BAD_GATEWAY.value(), "EXTERNAL_API_BUSINESS_ERROR",
					strategy.extractMessage(responseBody), traceId);
		}

		return new BufferingClientHttpResponseWrapper(response, responseBytes);
	}

	public static class BufferingClientHttpResponseWrapper implements ClientHttpResponse {

		private final ClientHttpResponse response;
		private final byte[] body;

		public BufferingClientHttpResponseWrapper(ClientHttpResponse response, byte[] body) {
			this.response = response;
			this.body = body;
		}

		@Override
		public InputStream getBody() {
			return new ByteArrayInputStream(body);
		}

		@Override
		public HttpStatusCode getStatusCode() throws IOException {
			return response.getStatusCode();
		}

		@Override
		public String getStatusText() throws IOException {
			return response.getStatusText();
		}

		@Override
		public void close() {
			response.close();
		}

		@Override
		public HttpHeaders getHeaders() {
			return response.getHeaders();
		}
	}
}
