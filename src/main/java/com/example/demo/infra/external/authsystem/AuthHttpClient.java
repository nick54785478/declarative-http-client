package com.example.demo.infra.external.authsystem;

import com.example.demo.application.shared.command.outbound.GetJwTokenFromAuthServiceCommand;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.example.demo.application.shared.dto.JwTokenGottenFromAuthServiceData;
import com.example.demo.application.shared.dto.UserInfoGottenFromAuthServiceData;

/**
 * Auth 外部系統 HTTP 介面 (Declarative HTTP Client)。
 *
 * <p>
 * 本介面利用 Spring 6 的 HTTP Interfaces 特性進行宣告式開發。
 * 開發者無須親自實作 HTTP 發送邏輯（如傳統的 RestTemplate 或 WebClient），
 * 只需透過 {@code @GetExchange}、{@code @PostExchange} 等註解定義路由與參數即可。
 * </p>
 *
 * <p>
 * 運作機制：
 * 系統啟動時，會透過 {@link com.example.demo.infra.httpclient.core.RestClientFactory} 
 * 搭配 {@code HttpServiceProxyFactory} 為此介面動態產生代理 (Proxy) 實作，
 * 並將所有橫切邏輯 (如攔截器、錯誤處理) 封裝在底層。
 * </p>
 */
public interface AuthHttpClient {

	/**
	 * 使用者登入，向外部 Auth 系統取得 JWT Token。
	 * 
	 * @param command 包含登入帳號、密碼等資訊的傳出指令 {@link GetJwTokenFromAuthServiceCommand}
	 * @return 回傳包含 token 字串的資料物件 {@link JwTokenGottenFromAuthServiceData}
	 */
	@PostExchange("/api/v1/login")
	JwTokenGottenFromAuthServiceData login(@RequestBody GetJwTokenFromAuthServiceCommand command);

	/**
	 * 透過使用者 ID 取得特定使用者的詳細資料。
	 * 
	 * @param id 欲查詢的使用者 ID
	 * @return 回傳該使用者的基本資料 {@link UserInfoGottenFromAuthServiceData}
	 */
	@GetExchange("/api/v1/users/{id}")
	UserInfoGottenFromAuthServiceData getUserById(@PathVariable Long id);

}
