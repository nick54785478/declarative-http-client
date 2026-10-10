package com.example.demo.infra.httpclient.feature.tracing.provider;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.infra.httpclient.core.WebClientFilterProvider;

import reactor.core.publisher.Mono;

/**
 * WebClient 追蹤碼過濾器提供者。
 *
 * <p>
 * 針對響應式架構，負責在 Header 注入 {@code X-Trace-Id}。
 * 若當前執行緒的 MDC 中已有 traceId，則繼承之；否則自動產生一組新的 UUID。
 * 注意：在純 WebFlux 環境中，MDC 的傳遞需依賴 Reactor Context 才能完美運作。
 * </p>
 */
@Component
public class TracingWebClientFilterProvider implements WebClientFilterProvider {

	private static final String TRACE_ID_HEADER = "X-Trace-Id";
	private static final String TRACE_ID_MDC_KEY = "traceId";

	@Override
	public boolean supports(String systemName) {
		return true;
	}

	@Override
	public void apply(WebClient.Builder builder, String systemName) {
		builder.filter(new ExchangeFilterFunction() {
			@Override
			public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
				String traceId = MDC.get(TRACE_ID_MDC_KEY);
				if (traceId == null) {
					traceId = UUID.randomUUID().toString();
				}

				ClientRequest newRequest = ClientRequest.from(request)
						.header(TRACE_ID_HEADER, traceId)
						.build();

				return next.exchange(newRequest);
			}
		});
	}
}