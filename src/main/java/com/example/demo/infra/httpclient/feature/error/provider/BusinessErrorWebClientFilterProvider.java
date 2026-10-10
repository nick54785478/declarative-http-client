package com.example.demo.infra.httpclient.feature.error.provider;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.application.shared.exception.ExternalApiException;
import com.example.demo.infra.httpclient.core.WebClientFilterProvider;
import com.example.demo.infra.httpclient.feature.error.business.BusinessErrorStrategy;
import com.example.demo.infra.httpclient.feature.error.registry.BusinessErrorStrategyRegistry;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * WebClient 業務錯誤過濾器提供者。
 *
 * <p>
 * 針對響應式架構，負責在 HTTP 2xx 回應中，讀取 Body 並交由 BusinessErrorStrategy 判斷是否為「偽裝成功」的業務錯誤。
 * 若為錯誤則拋出 ExternalApiException；若正常，則優雅地重建 ClientResponse 以供後續流程讀取。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class BusinessErrorWebClientFilterProvider implements WebClientFilterProvider {

	private final ExternalSystemProperties properties;
	private final BusinessErrorStrategyRegistry registry;

	@Override
	public boolean supports(String systemName) {
		ExternalSystemProperties.SystemConfig config = properties.getSystem(systemName);
		return config != null && Boolean.TRUE.equals(config.getEnableBusinessErrorHandling());
	}

	@Override
	public void apply(WebClient.Builder builder, String systemName) {
		builder.filter(new ExchangeFilterFunction() {
			@Override
			public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
				return next.exchange(request).flatMap(response -> {
					if (response.statusCode().is2xxSuccessful()) {
						BusinessErrorStrategy strategy = registry.getStrategy(systemName);
						if (strategy != null) {
							return response.bodyToMono(String.class)
									.defaultIfEmpty("")
									.flatMap(body -> {
										if (strategy.isBusinessError(body)) {
											String msg = strategy.extractMessage(body);
											return Mono.error(new ExternalApiException(systemName, response.statusCode().value(), "BUSINESS_ERROR", msg, "reactive-trace"));
										}
										ClientResponse newResponse = response.mutate()
												.body(body)
												.build();
										return Mono.just(newResponse);
									});
						}
					}
					return Mono.just(response);
				});
			}
		});
	}
}