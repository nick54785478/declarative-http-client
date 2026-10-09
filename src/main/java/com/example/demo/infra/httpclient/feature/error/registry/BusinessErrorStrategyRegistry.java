package com.example.demo.infra.httpclient.feature.error.registry;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.infra.httpclient.feature.error.business.BusinessErrorStrategy;
import com.example.demo.infra.httpclient.feature.error.business.impl.DefaultBusinessErrorStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * 業務錯誤策略註冊表 (Business Error Strategy Registry)。
 *
 * <p>
 * 集中管理所有的 {@link BusinessErrorStrategy} 實作，並根據系統名稱 (System Name)
 * 提供對應的策略實作。
 * </p>
 *
 * <p>
 * 設計模式 (Strategy & Registry Patterns)：
 * <ul>
 * <li>作為 Strategy Pattern 的 Context，負責策略的選擇與提供</li>
 * <li>將判斷業務錯誤的邏輯抽離，避免在呼叫端出現大量的 if-else 或 switch 判斷</li>
 * <li>提供預設策略 (Fallback)：若找不到對應策略，則回傳 {@link DefaultBusinessErrorStrategy}</li>
 * </ul>
 * </p>
 *
 * @see BusinessErrorStrategy
 * @see DefaultBusinessErrorStrategy
 */
@Slf4j
@Component
public class BusinessErrorStrategyRegistry {

	private final List<BusinessErrorStrategy> strategies;
	private final DefaultBusinessErrorStrategy defaultStrategy;

	public BusinessErrorStrategyRegistry(List<BusinessErrorStrategy> strategies,
			DefaultBusinessErrorStrategy defaultStrategy) {

		this.strategies = strategies;
		this.defaultStrategy = defaultStrategy;
	}

	/**
	 * 根據系統名稱取得對應策略。
	 *
	 * @param systemName 系統名稱
	 * @return BusinessErrorStrategy，若找不到則回傳預設策略
	 */
	public BusinessErrorStrategy getStrategy(String systemName) {

		for (BusinessErrorStrategy strategy : strategies) {
			log.info("Checking strategy: {}", strategy.getClass().getSimpleName());
			if (strategy.supports(systemName)) {
				log.info("Matched strategy: {}", strategy.getClass().getSimpleName());
				return strategy;
			}
		}

		log.info("Using default strategy");
		return defaultStrategy;
	}
}
