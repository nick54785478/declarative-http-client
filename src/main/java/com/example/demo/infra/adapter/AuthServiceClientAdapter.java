package com.example.demo.infra.adapter;

import com.example.demo.infra.external.authsystem.AuthHttpClient;
import org.springframework.stereotype.Component;

import com.example.demo.application.port.out.AuthServiceClientPort;
import com.example.demo.application.shared.command.outbound.GetJwTokenFromAuthServiceCommand;
import com.example.demo.application.shared.dto.JwTokenGottenFromAuthServiceData;
import com.example.demo.application.shared.dto.UserInfoGottenFromAuthServiceData;

import lombok.RequiredArgsConstructor;

/**
 * Auth 系統的 Outbound Adapter 實作。
 *
 * <p>
 * 負責實作 Application 層的 {@link AuthServiceClientPort}，
 * 並將請求委派給底層的宣告式客戶端 {@link AuthHttpClient}。
 * 隔離了業務邏輯與基礎建設的 HTTP 實作細節。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class AuthServiceClientAdapter implements AuthServiceClientPort {

	private final AuthHttpClient authHttpClient;

	@Override
	public JwTokenGottenFromAuthServiceData login(GetJwTokenFromAuthServiceCommand command) {
		return authHttpClient.login(command);
	}

	@Override
	public UserInfoGottenFromAuthServiceData getUserById(Long id) {
		return authHttpClient.getUserById(id);
	}
}
