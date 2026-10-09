package com.example.demo.infra.httpclient.feature.auth.strategy.impl;

import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.auth.strategy.AuthStrategy;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Basic 認證策略
 *
 * <p>
 * 當系統設定 authType=basic 時使用。從 ExternalSystemProperties 取得 username 與 password，
 * 寫入 HTTP Request 的 Authorization Header。
 * </p>
 */
@Slf4j
@Component
public class BasicAuthStrategy implements AuthStrategy {

	@Override
	public boolean supports(String authType) {
		return "basic".equalsIgnoreCase(authType);
	}

	@Override
	public void apply(HttpRequest request, ExternalSystemProperties.SystemConfig config) {
		request.getHeaders().setBasicAuth(config.getUsername(), config.getPassword());
		log.debug("[Auth] Using Basic authentication");
	}
}
