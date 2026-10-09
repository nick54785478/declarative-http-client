package com.example.demo.infra.httpclient.feature.header.factory;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.header.resolver.HeaderValueResolver;

import lombok.RequiredArgsConstructor;

/**
 * Header 值解析工廠。
 *
 * <p>
 * 負責解析 HTTP Header 設定的動態值，例如：
 * <ul>
 * <li>${uuid} 動態轉換為隨機 UUID</li>
 * <li>${timestamp} 動態轉換為目前時間戳</li>
 * <li>靜態字串 則保持原值</li>
 * </ul>
 * </p>
 *
 * <p>
 * 解析流程：
 * <ul>
 * <li>遍歷所有 {@link HeaderValueResolver} 實作，判斷哪個 resolver 支援該 value</li>
 * <li>找到第一個支援的 resolver 並呼叫 {@link HeaderValueResolver#resolve(String)}</li>
 * <li>如果找不到支援的 resolver，則拋出 {@link IllegalStateException}</li>
 * </ul>
 * </p>
 *
 * <p>
 * 使用範例：
 * 
 * <pre>{@code
 * @Autowired
 * private HeaderValueResolverFactory factory;
 *
 * String resolvedValue = factory.resolve("${uuid}"); // 產生隨機 UUID
 * String staticValue = factory.resolve("my-client"); // 保持原值
 * }</pre>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class HeaderValueResolverFactory {

	private final List<HeaderValueResolver> resolvers;

	public String resolve(String value) {
		return resolvers.stream().filter(resolver -> resolver.supports(value)).findFirst()
				.orElseThrow(() -> new IllegalStateException("No resolver found for value: " + value)).resolve(value);
	}
}
