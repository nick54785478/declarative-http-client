package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.example.demo.infra.external.authsystem.AuthHttpClient;
import com.example.demo.infra.httpclient.core.RestClientFactory;
import com.example.demo.infra.httpclient.core.WebClientFactory;

/**
 * HTTP Client Configuration.
 *
 * <p>
 * 此設定類別負責建立各個外部系統的 HttpClient Bean。透過 {@link RestClientFactory} 建立
 * RestClient，再利用 {@link HttpServiceProxyFactory} 將 RestClient 轉換為動態代理介面
 * {@link AuthHttpClient}，以便在 Service/Controller 中直接呼叫方法。
 * </p>
 *
 * <p>
 * 建立流程：
 * <ol>
 * <li>從 {@link RestClientFactory} 取得基礎的 RestClient，已套用各系統的 Interceptor</li>
 * <li>使用 {@link RestClientAdapter#create} 建立配接器 RestClientAdapter</li>
 * <li>利用 {@link HttpServiceProxyFactory} 建構代理工廠，並產生動態代理介面</li>
 * <li>註冊為 Bean，供 Spring 注入使用</li>
 * </ol>
 * </p>
 *
 * <p>
 * 優點：
 * <ul>
 * <li>各個外部系統的 HttpClient 皆為強型別介面，直接定義 API 簽章</li>
 * <li>Interceptor（如認證、Logging、Retry 等）在 RestClient 層統一處理</li>
 * <li>Service 層只需注入介面，無須操作底層的 RestTemplate 或 RestClient</li>
 * </ul>
 * </p>
 */
@Configuration
public class HttpClientConfiguration {

	@Value("${external.client-type:REST_CLIENT}")
	private String clientType;

	/**
	 * AuthService HttpClient Bean。
	 *
	 * <p>
	 * 透過 RestClientFactory 或 WebClientFactory 建立 auth 專用的 Client，並轉換為動態代理介面 {@link AuthHttpClient}。
	 * </p>
	 *
	 * @param restFactory RestClient 工廠
	 * @param webFactory  WebClient 工廠
	 * @return {@link AuthHttpClient} Bean
	 */
	@Bean
	public AuthHttpClient authHttpClient(RestClientFactory restFactory, WebClientFactory webFactory) {
		HttpServiceProxyFactory proxyFactory;
		
		if ("WEB_CLIENT".equalsIgnoreCase(clientType)) {
			// 使用 WebClient (非同步/響應式)
			proxyFactory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webFactory.create("auth"))).build();
		} else {
			// 使用 RestClient (同步)
			proxyFactory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restFactory.create("auth"))).build();
		}
		
		return proxyFactory.createClient(AuthHttpClient.class);
	}

//	/**
//	 * sqms8d 系統 HttpClient Bean。
//	 *
//	 * <p>
//	 * 透過 RestClientFactory 建立 sqms8d 專用的 RestClient，並轉換為動態代理介面
//	 * {@link Sqms8dHttpClient}。
//	 * </p>
//	 *
//	 * @param factory RestClient 工廠
//	 * @return {@link Sqms8dHttpClient} Bean
//	 */
//	@Bean
//	public Sqms8dHttpClient sqms8dHttpClient(RestClientFactory factory) {
//		return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(factory.create("sqms8d"))).build()
//				.createClient(Sqms8dHttpClient.class);
//	}
}
