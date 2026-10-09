package com.example.demo.infra.httpclient.feature.error.factory;

import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.error.business.BusinessErrorStrategy;
import com.example.demo.infra.httpclient.feature.error.business.impl.DefaultBusinessErrorStrategy;
import com.example.demo.infra.httpclient.feature.error.interceptor.BusinessErrorInterceptor;
import com.example.demo.infra.httpclient.feature.error.registry.BusinessErrorStrategyRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 工廠：建立 {@link BusinessErrorInterceptor}。
 *
 * <p>
 * 負責向 {@link BusinessErrorStrategyRegistry} 查詢對應策略，若找不到則退回使用
 * {@link DefaultBusinessErrorStrategy}。
 * </p>
 *
 * <p>
 * 職責：
 * <ul>
 * <li>取得對應系統的 BusinessErrorStrategy</li>
 * <li>建立並回傳 BusinessErrorInterceptor</li>
 * <li>處理找不到策略時的預設行為，並記錄警告</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessErrorInterceptorFactory {

	private final BusinessErrorStrategyRegistry registry;
	private final DefaultBusinessErrorStrategy defaultStrategy;

	/**
	 * 建立指定系統的 {@link BusinessErrorInterceptor}。
	 *
	 * @param systemName 系統名稱
	 * @return BusinessErrorInterceptor 實體
	 */
	public BusinessErrorInterceptor create(String systemName) {
		BusinessErrorStrategy strategy = registry.getStrategy(systemName);

		if (strategy == null) {
			log.warn("[BusinessErrorInterceptorFactory] system={} strategy not found, using default", systemName);
			strategy = defaultStrategy;
		}

		log.info("[BusinessErrorInterceptorFactory] Creating interceptor for system={} with strategy={}", systemName,
				strategy.getClass().getSimpleName());

		return new BusinessErrorInterceptor(systemName, strategy);
	}
}
