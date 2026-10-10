package com.example.demo.infra.httpclient.feature.error.provider;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.application.shared.exception.ExternalApiException;
import com.example.demo.infra.httpclient.core.WebClientFilterProvider;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * WebClient HTTP 異常過濾器提供者。
 *
 * <p>
 * 針對響應式架構，負責攔截 HTTP 4xx 與 5xx 錯誤。
 * 將非同步回應的 Body 讀取出來後，統一封裝成系統標準的 ExternalApiException 並拋出 (Mono.error)。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ErrorWebClientFilterProvider implements WebClientFilterProvider {

	@Override
	public boolean supports(String systemName) {
		return true; 
	}

	@Override
	public void apply(WebClient.Builder builder, String systemName) {
		builder.filter(new ExchangeFilterFunction() {
			@Override
			public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
				return next.exchange(request).flatMap(response -> {
					if (response.statusCode().isError()) {
						return response.bodyToMono(String.class)
								.defaultIfEmpty("")
								.flatMap(body -> Mono.error(new ExternalApiException(systemName, response.statusCode().value(), "WEBCLIENT_ERROR", body, "reactive-trace")));
					}
					return Mono.just(response);
				});
			}
		});
	}
}