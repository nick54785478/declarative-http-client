package com.example.demo.infra.httpclient.feature.retry.provider;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.demo.infra.httpclient.core.WebClientFilterProvider;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * WebClient 重試過濾器提供者。
 *
 * <p>
 * 針對響應式架構，利用 WebFlux 內建的 {@code retryWhen} 實作非阻塞的重試機制。
 * 當遭遇到 HTTP 5xx Server Error 時，自動依據設定的重試次數與延遲時間進行重試。
 * </p>
 */
@Slf4j
@Component
public class RetryWebClientFilterProvider implements WebClientFilterProvider {

	@Value("${external.retry.max-attempts:3}")
	private int maxAttempts;

	@Value("${external.retry.delay-millis:1000}")
	private long delayMillis;

	@Override
	public boolean supports(String systemName) {
		return true;
	}

	@Override
	public void apply(WebClient.Builder builder, String systemName) {
		builder.filter(new ExchangeFilterFunction() {
			@Override
			public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
				return next.exchange(request)
						.flatMap(response -> {
							if (response.statusCode().is5xxServerError()) {
								return Mono.error(new RuntimeException("Server Error " + response.statusCode()));
							}
							return Mono.just(response);
						})
						.retryWhen(Retry.fixedDelay(maxAttempts, Duration.ofMillis(delayMillis))
								.doBeforeRetry(retrySignal -> log.warn("[HTTP-Retry] [{}] {} {} - Attempt {}", 
										systemName, request.method(), request.url(), retrySignal.totalRetries() + 1)));
			}
		});
	}
}