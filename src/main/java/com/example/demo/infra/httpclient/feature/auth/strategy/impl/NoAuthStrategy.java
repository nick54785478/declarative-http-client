package com.example.demo.infra.httpclient.feature.auth.strategy.impl;

import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.auth.strategy.AuthStrategy;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * 無認證策略 (None)
 *
 * <p>
 * 當系統未設定認證類型，或是 authType=none 時使用。不在 HTTP Request 加入任何 Authorization Header。
 * </p>
 */
@Slf4j
@Component
public class NoAuthStrategy implements AuthStrategy {

	@Override
	public boolean supports(String authType) {
		return authType == null || "none".equalsIgnoreCase(authType);
	}

	@Override
	public void apply(HttpRequest request, ExternalSystemProperties.SystemConfig config) {
		log.debug("[Auth] No authentication required");
	}
}
