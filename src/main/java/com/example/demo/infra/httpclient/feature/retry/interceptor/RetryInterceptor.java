package com.example.demo.infra.httpclient.feature.retry.interceptor;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP 重試攔截器。
 *
 * <p>
 * 負責攔截 HTTP 請求進行重試，適用於短暫性錯誤（目前僅針對 5xx Server Error 重試）。
 * </p>
 *
 * <p>
 * 流程：
 * <ol>
 * <li>發送 HTTP 請求</li>
 * <li>若失敗，檢查是否符合 {@link #shouldRetry(ClientHttpResponse)}，若是則進行重試</li>
 * <li>重試次數由 {@code maxRetries} 決定，每次間隔 {@code retryDelayMillis} 毫秒</li>
 * <li>若超過最大重試次數，則拋出最後一次例外或回傳</li>
 * </ol>
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class RetryInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * 系統名稱，用於日誌追蹤。
	 */
	private final String systemName;

	/**
	 * 最大重試次數。
	 */
	private final int maxRetries;

	/**
	 * 每次重試間隔（毫秒）。
	 */
	private final long retryDelayMillis;

	/**
	 * 攔截 HTTP 請求並在需要時進行重試。
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

		for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {

			try {
				ClientHttpResponse response = execution.execute(request, body);

				if (!shouldRetry(response) || attempt > maxRetries) {
					return response;
				}

				log.warn("[RETRY] system={} attempt={} status={} - retrying in {}ms", systemName, attempt,
						response.getStatusCode(), retryDelayMillis);

				sleepUninterruptibly(retryDelayMillis);

			} catch (IOException | RuntimeException ex) {

				if (attempt > maxRetries) {
					log.error("[RETRY] system={} attempt={} failed, giving up", systemName, attempt, ex);
					throw ex;
				}

				log.warn("[RETRY] system={} attempt={} exception, retrying in {}ms", systemName, attempt,
						retryDelayMillis, ex);

				sleepUninterruptibly(retryDelayMillis);
			}
		}

		throw new IllegalStateException("Should never reach here");
	}

	/**
	 * 判斷是否需要重試。
	 *
	 * <p>
	 * 目前僅針對 HTTP 5xx Server Error 進行重試。
	 * </p>
	 *
	 * @param response HTTP 回應
	 * @return true: 需要重試, false: 不重試
	 * @throws IOException 讀取狀態碼時可能拋出
	 */
	private boolean shouldRetry(ClientHttpResponse response) throws IOException {
		return response.getStatusCode().is5xxServerError();
	}

	/**
	 * 不可中斷的睡眠。
	 *
	 * @param millis 睡眠毫秒數
	 */
	private void sleepUninterruptibly(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) {
			log.warn("InterruptedException ignored");
		}
	}
}
