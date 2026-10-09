package com.example.demo.infra.httpclient.feature.auth.strategy.impl;

import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.auth.strategy.AuthStrategy;
import com.example.demo.application.shared.context.ContextHolder;
import com.example.demo.infra.shared.properties.ExternalSystemProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * JWT 認證策略
 *
 * <p>
 * 當系統設定 authType=jwt 時使用。從 ContextHolder 取得 JWT Token 並寫入 Authorization
 * Header。
 * </p>
 */
@Slf4j
@Component
public class JwtAuthStrategy implements AuthStrategy {

	@Override
	public boolean supports(String authType) {
		return "jwt".equalsIgnoreCase(authType);
	}

	@Override
	public void apply(HttpRequest request, ExternalSystemProperties.SystemConfig config) {
		String token = ContextHolder.getJwtoken();
		request.getHeaders().setBearerAuth(token);
		log.debug("[Auth] Using JWT authentication");
	}
}
