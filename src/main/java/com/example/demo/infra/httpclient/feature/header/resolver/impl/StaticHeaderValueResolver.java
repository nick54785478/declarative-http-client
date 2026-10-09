package com.example.demo.infra.httpclient.feature.header.resolver.impl;

import com.example.demo.infra.httpclient.feature.header.factory.HeaderValueResolverFactory;
import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.header.resolver.HeaderValueResolver;

/**
 * 靜態 Header 值解析器。
 *
 * <p>
 * 作為最後一線的預設解析器 (fallback)，當 {@link HeaderValueResolverFactory}
 * 找不到其他符合的解析器時，原封不動回傳原本的設定字串。
 * </p>
 */
@Component
public class StaticHeaderValueResolver implements HeaderValueResolver {

	@Override
	public boolean supports(String value) {
		return true; // fallback resolver, 永遠支援
	}

	@Override
	public String resolve(String value) {
		return value; // 回傳原本字串
	}
}
