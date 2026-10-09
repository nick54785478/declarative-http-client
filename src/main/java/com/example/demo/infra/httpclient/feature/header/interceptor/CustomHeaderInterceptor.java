package com.example.demo.infra.httpclient.feature.header.interceptor;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.example.demo.infra.httpclient.feature.header.factory.HeaderValueResolverFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 自訂 Header 攔截器。
 *
 * <p>
 * 此攔截器負責在 HTTP 請求發送前，依據系統設定的自訂 Header 注入到請求中。支援動態 Header 值的解析（如
 * ${uuid}、${timestamp} 等）。
 * </p>
 *
 * <p>
 * 流程：
 * <ol>
 * <li>取得系統設定的 {@code customHeaders}</li>
 * <li>透過 {@link HeaderValueResolverFactory} 解析每個值</li>
 * <li>將解析後的值加入 HTTP Request Header</li>
 * <li>印出 debug 日誌，記錄新增的 Header 資訊</li>
 * </ol>
 * </p>
 *
 * <p>
 * 注意：
 * <ul>
 * <li>此類僅負責單一系統的 Header 注入</li>
 * <li>是否掛載攔截由 Provider 控制，不在此類範圍</li>
 * </ul>
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class CustomHeaderInterceptor implements ClientHttpRequestInterceptor {

	private final String systemName;
	private final Map<String, String> customHeaders;
	private final HeaderValueResolverFactory resolverFactory;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		customHeaders.forEach((key, value) -> {
			String resolvedValue = resolverFactory.resolve(value);
			request.getHeaders().add(key, resolvedValue);
			log.debug("[CustomHeader] system={} added {}={}", systemName, key, resolvedValue);
		});

		return execution.execute(request, body);
	}
}
