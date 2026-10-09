package com.example.demo.application.shared.context;

import lombok.extern.slf4j.Slf4j;

/**
 * 上下文工具類
 */
public class ContextHolder {

	/**
	 * 儲存目前使用者傳入的 JWT Token
	 */
	private static final ThreadLocal<String> JWT_TOKEN = new ThreadLocal<>();

	/**
	 * 把 JWT Token 設定到 ThreadLocal 內
	 * 
	 * @param token
	 */
	public static void setJwtToken(String token) {
		JWT_TOKEN.set(token);
	}

	/**
	 * 取得目前登入者的 JwToken
	 * 
	 * @return token
	 */
	public static String getJwtoken() {
		return JWT_TOKEN.get() != null ? JWT_TOKEN.get() : null;
	}

	/**
	 * 清理上下文
	 */
	public static void clear() {
		JWT_TOKEN.remove();
	}

}