package com.example.demo.infra.httpclient.feature.error.business.impl;

import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.error.business.BusinessErrorStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 預設業務錯誤策略。
 *
 * <p>
 * 當系統未定義專用策略時作為備案。
 * 判斷邏輯：若 JSON 回應包含 {@code "success": false} 則視為業務錯誤。
 * </p>
 */
@Slf4j
@Component
public class DefaultBusinessErrorStrategy implements BusinessErrorStrategy {

    private final ObjectMapper objectMapper;

    public DefaultBusinessErrorStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String systemName) {
        log.info("[BusinessErrorStrategy] DefaultBusinessErrorStrategy");
        return false; // 預設不主動匹配
    }

    @Override
    public boolean isBusinessError(String responseBody) {
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            return node.has("success") && !node.get("success").asBoolean();
        } catch (Exception e) {
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
