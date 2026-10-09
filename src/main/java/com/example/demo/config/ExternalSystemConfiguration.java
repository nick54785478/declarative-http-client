package com.example.demo.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.infra.httpclient.feature.error.handler.ExternalApiErrorHandler;
import com.example.demo.infra.httpclient.feature.error.handler.impl.DefaultExternalApiErrorHandler;
import com.example.demo.infra.httpclient.feature.error.registry.ExternalApiErrorHandlerRegistry;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

/**
 * External System Configuration。
 *
 * <p>
 * 此設定類別啟用 {@link ExternalSystemProperties}，將 application.properties 或
 * application.yml 中的 external 系統相關設定載入為 Spring 容器中的 Bean，供後續 RestClient
 * 或攔截器使用。
 * </p>
 *
 * <p>
 * 職責：
 * <ul>
 * <li>將 external 系統設定（如 baseUrl、認證資訊 JWT / Basic、自訂 Header 等）映射為強型別 POJO</li>
 * <li>讓其他元件可以透過依賴注入取得 {@link ExternalSystemProperties}</li>
 * <li>註冊 {@link ExternalApiErrorHandlerRegistry}，以管理各系統的錯誤處理器</li>
 * </ul>
 * </p>
 *
 * <p>
 * 使用範例：
 * 
 * <pre>
 * {@code
 * @Autowired
 * private ExternalSystemProperties externalSystemProperties;
 *
 * String baseUrl = externalSystemProperties.getBaseUrl("sqms8d");
 * boolean isJwt = externalSystemProperties.isJwtEnabled("sqms8d");
 * }
 * </pre>
 * </p>
 */
@Configuration
@EnableConfigurationProperties(ExternalSystemProperties.class)
public class ExternalSystemConfiguration {

	/**
	 * 註冊 ExternalApiErrorHandlerRegistry。
	 *
	 * <p>
	 * 將所有非預設的 ExternalApiErrorHandler 註冊到 Registry，並指定
	 * DefaultExternalApiErrorHandler 作為 fallback。
	 * </p>
	 *
	 * @param allHandlers    Spring 容器中所有的 ExternalApiErrorHandler
	 * @param defaultHandler 預設的錯誤處理器
	 * @return ExternalApiErrorHandlerRegistry
	 */
	@Bean
	public ExternalApiErrorHandlerRegistry externalApiErrorHandlerRegistry(List<ExternalApiErrorHandler> allHandlers,
			DefaultExternalApiErrorHandler defaultHandler) {

		// 排除 DefaultExternalApiErrorHandler，避免重複註冊
		List<ExternalApiErrorHandler> handlers = allHandlers.stream()
				.filter(h -> !(h instanceof DefaultExternalApiErrorHandler)).toList();

		return new ExternalApiErrorHandlerRegistry(handlers, defaultHandler);
	}
}
