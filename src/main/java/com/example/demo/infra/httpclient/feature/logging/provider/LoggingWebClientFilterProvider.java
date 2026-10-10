package com.example.demo.infra.httpclient.feature.logging.provider;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.infra.httpclient.core.WebClientFilterProvider;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * WebClient 日誌過濾器提供者。
 *
 * <p>
 * 針對響應式架構，負責在請求發出前記錄 Method 與 URL，
 * 並利用 WebFlux 的 {@code doOnSuccess} 與 {@code doOnError}，
 * 精準記錄非阻塞呼叫的執行時間與最終狀態碼。
 * </p>
 */
@Slf4j
@Component
public class LoggingWebClientFilterProvider implements WebClientFilterProvider {

	@Override
	public boolean supports(String systemName) {
		return true; 
	}

	@Override
	public void apply(WebClient.Builder builder, String systemName) {
		builder.filter(new ExchangeFilterFunction() {
			@Override
			public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
				long startTime = System.currentTimeMillis();
				log.info("[HTTP-Req] [{}] {} {}", systemName, request.method(), request.url());
				
				return next.exchange(request).doOnSuccess(response -> {
					long duration = System.currentTimeMillis() - startTime;
					log.info("[HTTP-Res] [{}] {} {} - Status: {} ({}ms)", 
							systemName, request.method(), request.url(), response.statusCode(), duration);
				}).doOnError(error -> {
					long duration = System.currentTimeMillis() - startTime;
					log.error("[HTTP-Err] [{}] {} {} - Error: {} ({}ms)", 
							systemName, request.method(), request.url(), error.getMessage(), duration);
				});
			}
		});
	}
}