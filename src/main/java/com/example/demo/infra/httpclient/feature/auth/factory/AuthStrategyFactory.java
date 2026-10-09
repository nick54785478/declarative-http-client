package com.example.demo.infra.httpclient.feature.auth.factory;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.auth.strategy.AuthStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 認證策略工廠
 *
 * <p>
 * 負責根據外部系統設定的 authType（如 "jwt", "basic", "none"）提供對應的 {@link AuthStrategy} 實作。
 * </p>
 *
 * <p>
 * 透過建構子注入 Spring 容器中所有的 {@link List<AuthStrategy>} 實作，並在執行期動態決定要套用哪一種策略來處理 HTTP Request。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthStrategyFactory {

	private final List<AuthStrategy> strategies;

	/**
	 * 根據 authType 取得對應的策略實作。
	 *
	 * @param authType 認證類型，例如 "jwt", "basic", "none"
	 * @return 對應的 {@link AuthStrategy} 實作
	 * @throws IllegalStateException 若找不到支援的 authType
	 */
	public AuthStrategy getStrategy(String authType) {
		return strategies.stream().filter(strategy -> strategy.supports(authType)).findFirst()
				.orElseThrow(() -> new IllegalStateException("Unsupported authType: " + authType));
	}
}
