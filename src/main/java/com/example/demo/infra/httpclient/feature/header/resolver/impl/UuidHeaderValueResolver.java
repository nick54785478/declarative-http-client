package com.example.demo.infra.httpclient.feature.header.resolver.impl;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.header.resolver.HeaderValueResolver;

/**
 * UUID Header 值解析器。
 *
 * <p>
 * 當設定值為 "${uuid}" 時，動態產生並回傳新的 UUID。
 * </p>
 */
@Component
public class UuidHeaderValueResolver implements HeaderValueResolver {

	@Override
	public boolean supports(String value) {
		return "${uuid}".equalsIgnoreCase(value);
	}

	@Override
	public String resolve(String value) {
		return UUID.randomUUID().toString();
	}
}
