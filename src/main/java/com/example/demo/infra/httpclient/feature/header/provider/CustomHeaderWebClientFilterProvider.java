package com.example.demo.infra.httpclient.feature.header.provider;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.infra.httpclient.core.WebClientFilterProvider;
import com.example.demo.infra.httpclient.feature.header.factory.HeaderValueResolverFactory;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * WebClient 自訂 Header 過濾器提供者。
 *
 * <p>
 * 針對響應式架構，負責讀取 {@code external.systems.<name>.headers} 設定，
 * 並透過 HeaderValueResolverFactory 動態解析參數 (如固定字串或 UUID)，
 * 自動注入到每個 HTTP Request 的 Header 中。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CustomHeaderWebClientFilterProvider implements WebClientFilterProvider {

	private final ExternalSystemProperties properties;
	private final HeaderValueResolverFactory resolverFactory;

	@Override
	public boolean supports(String systemName) {
		ExternalSystemProperties.SystemConfig config = properties.getSystems().get(systemName);
		return config != null && config.getHeaders() != null && !config.getHeaders().isEmpty();
	}

	@Override
	public void apply(WebClient.Builder builder, String systemName) {
		ExternalSystemProperties.SystemConfig config = properties.getSystems().get(systemName);
		
		builder.filter(new ExchangeFilterFunction() {
			@Override
			public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
				ClientRequest.Builder requestBuilder = ClientRequest.from(request);
				
				config.getHeaders().forEach((key, valuePattern) -> {
					String resolvedValue = resolverFactory.resolve(valuePattern);
					requestBuilder.header(key, resolvedValue);
				});

				return next.exchange(requestBuilder.build());
			}
		});
	}
}