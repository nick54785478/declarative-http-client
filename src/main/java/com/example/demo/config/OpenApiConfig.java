package com.example.demo.config;

import org.springdoc.core.customizers.OperationCustomizer;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI (Swagger) 視覺化文件設定檔。
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "bearerAuth";
		
		return new OpenAPI()
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(
						new Components()
								.addSecuritySchemes(securitySchemeName,
										new SecurityScheme()
												.name(securitySchemeName)
												.type(SecurityScheme.Type.HTTP)
												.scheme("bearer")
												.bearerFormat("JWT")
												.description("請在此輸入 JWT Token")
								)
				)
				.info(new Info()
						.title("HTTP Interface Demo API")
						.version("1.0.0")
						.description("展示 Spring 6 HTTP Interfaces 搭配 RestClient 的企業級外部系統介接架構範例。")
						.contact(new Contact().name("開發團隊")));
	}

	/**
	 * 讓所有 API 都能在 Swagger UI 上手動輸入額外的 Header (類似 Postman)。
	 */
	@Bean
	public OperationCustomizer customGlobalHeaders() {
		return (operation, handlerMethod) -> {
			operation.addParametersItem(new Parameter()
					.in("header")
					.name("X-Custom-Header")
					.description("手動配置的全域自定義 Header (類似 Postman，不填則略過)")
					.required(false));
			return operation;
		};
	}
}
