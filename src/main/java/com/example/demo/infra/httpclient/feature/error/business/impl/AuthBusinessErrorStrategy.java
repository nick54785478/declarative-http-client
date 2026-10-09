package com.example.demo.infra.httpclient.feature.error.business.impl;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.error.business.BusinessErrorStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * AuthService 專用業務錯誤策略。
 *
 * <p>
 * 判斷邏輯：
 * <ul>
 * <li>僅支援 {@code systemName="auth"}</li>
 * <li>解析回應 JSON，若 {@code code} 非 {@code 200, 201} 則視為業務錯誤</li>
 * <li>萃取 {@code message} 欄位作為錯誤訊息</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class AuthBusinessErrorStrategy implements BusinessErrorStrategy {

	private static final String SYSTEM_NAME = "auth";
	private static final Set<String> SUCCESS_CODES = Set.of("200", "201");

	private final ObjectMapper objectMapper;

	public AuthBusinessErrorStrategy(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean supports(String systemName) {
		return SYSTEM_NAME.equalsIgnoreCase(systemName);
	}

	@Override
	public boolean isBusinessError(String responseBody) {
		try {
			JsonNode node = objectMapper.readTree(responseBody);

			if (!node.has("code")) {
				return false;
			}

			String code = node.get("code").asText();
			return !SUCCESS_CODES.contains(code);

		} catch (Exception e) {
			log.warn("[Auth] Failed to parse response body", e);
			return false;
		}
	}

	@Override
	public String extractMessage(String responseBody) {
		try {
			JsonNode node = objectMapper.readTree(responseBody);
			return node.has("message") ? node.get("message").asText() : responseBody;
		} catch (Exception e) {
			return responseBody;
		}
	}
}
