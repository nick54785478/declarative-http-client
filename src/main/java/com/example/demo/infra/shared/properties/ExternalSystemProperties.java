package com.example.demo.infra.shared.properties;


import java.util.Collections;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 外部系統設定屬性。
 *
 * <p>
 * 對應 application.properties 中的 external.* 設定， 用於統一管理所有外部 API 的：
 * <ul>
 * <li>Base URL</li>
 * <li>認證類型 (jwt / basic / none)</li>
 * <li>Basic Auth 帳密</li>
 * <li>自定義 Request Header</li>
 * </ul>
 * </p>
 *
 * <p>
 * 範例：
 * 
 * <pre>
 * external.systems.sqms8d.base-url=https://api.sqms8d.com
 * external.systems.sqms8d.auth-type=jwt
 *
 * external.systems.erp.base-url=https://api.erp.com
 * external.systems.erp.auth-type=basic
 * external.systems.erp.username=erp_user
 * external.systems.erp.password=erp_password
 *
 * external.systems.erp.headers.X-Api-Version=1.0
 * </pre>
 * </p>
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = "external")
public class ExternalSystemProperties {

	/**
	 * 所有外部系統設定。
	 * <p>
	 * key = systemName value = 該系統設定
	 */
	private Map<String, SystemConfig> systems = Collections.emptyMap();

	/**
	 * 取得指定系統設定。
	 *
	 * @param systemName 系統名稱
	 * @return 系統設定
	 */
	public SystemConfig getSystem(String systemName) {
		SystemConfig config = systems.get(systemName);
		if (config == null) {
			log.error("Unknown system: {}", systemName);
			throw new IllegalArgumentException("Unknown system: " + systemName);
		}
		return config;
	}

	/**
	 * 取得指定系統 baseUrl。
	 */
	public String getBaseUrl(String systemName) {
		return getSystem(systemName).getBaseUrl();
	}

	/**
	 * 取得指定系統自定義 Header。
	 */
	public Map<String, String> getCustomHeaders(String systemName) {
		return getSystem(systemName).getHeaders();
	}

	/**
	 * 判斷該系統是否使用 JWT。
	 */
	public boolean isJwtEnabled(String systemName) {
		return "jwt".equalsIgnoreCase(getSystem(systemName).getAuthType());
	}

	/**
	 * 判斷該系統是否使用 Basic Auth。
	 */
	public boolean isBasicAuthEnabled(String systemName) {
		return "basic".equalsIgnoreCase(getSystem(systemName).getAuthType());
	}

	/**
	 * 單一外部系統設定。
	 */
	@Getter
	@Setter
	public static class SystemConfig {

		/**
		 * 系統 Base URL。
		 */
		private String baseUrl;

		/**
		 * 認證類型：
		 * <ul>
		 * <li>jwt</li>
		 * <li>basic</li>
		 * <li>none</li>
		 * </ul>
		 */
		private String authType = "none";

		/**
		 * Basic Auth 使用者名稱。
		 */
		private String username;

		/**
		 * Basic Auth 密碼。
		 */
		private String password;

		/**
		 * 該系統專屬自定義 Header。
		 * <p>
		 * key = header name value = header value
		 */
		private Map<String, String> headers = Collections.emptyMap();

		/**
		 * 是否啟用業務錯誤處理 (預設為 false)。
		 */
		private Boolean enableBusinessErrorHandling = false;
	}
}
