package com.example.demo.infra.httpclient.feature.auth.provider;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.infra.httpclient.core.WebClientFilterProvider;
import com.example.demo.infra.httpclient.feature.auth.factory.AuthStrategyFactory;
import com.example.demo.infra.httpclient.feature.auth.strategy.AuthStrategy;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;

/**
 * WebClient 認證過濾器提供者。
 *
 * <p>
 * 針對響應式架構，負責讀取 {@code external.systems.<name>.auth-type} 設定，
 * 並透過 AuthStrategyFactory 套用對應的認證邏輯 (如 JWT、Basic)。
 * 為了重複利用現有的同步 AuthStrategy，內部實作了匿名的 HttpRequest 作為轉接。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationWebClientFilterProvider implements WebClientFilterProvider {

	private final ExternalSystemProperties properties;
	private final AuthStrategyFactory authStrategyFactory;

	@Override
	public boolean supports(String systemName) {
		return true;
	}

	@Override
	public void apply(WebClient.Builder builder, String systemName) {
		ExternalSystemProperties.SystemConfig config = properties.getSystems().get(systemName);
		if (config == null || "none".equalsIgnoreCase(config.getAuthType())) {
			return;
		}

		builder.filter(new ExchangeFilterFunction() {
			@Override
			public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
				AuthStrategy strategy = authStrategyFactory.getStrategy(config.getAuthType());
				
				HttpRequest mockRequest = new HttpRequest() {
					private HttpHeaders headers = new HttpHeaders();
					@Override public HttpHeaders getHeaders() { return headers; }
					@Override public HttpMethod getMethod() { return request.method(); }
					@Override public URI getURI() { return request.url(); }
					@Override public Map<String, Object> getAttributes() { return Collections.emptyMap(); }
				};
				
				strategy.apply(mockRequest, config);

				ClientRequest newRequest = ClientRequest.from(request)
						.headers(headers -> headers.addAll(mockRequest.getHeaders()))
						.build();

				return next.exchange(newRequest);
			}
		});
	}
}