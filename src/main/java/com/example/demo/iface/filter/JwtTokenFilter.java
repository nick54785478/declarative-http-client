package com.example.demo.iface.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.application.shared.context.ContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 用於攔截所有的 HTTP Request，從 Header 中提取授權資訊， 將其轉換為 JWT（JSON Web Token）後， 存儲到
 * ContextHolder 內以供後續使用。
 * <p>
 * 註. 目前只驗有沒有 Token
 * </p>
 */
@Slf4j
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

	/**
	 * 公開的路徑列表，不需要進行 JWT 驗證的路徑。
	 */
	private static final String[] PUBLIC_PATHS = { "/health", "/favicon.ico", "**/api-docs/**", "**/swagger-ui**",
			"/api/v1/auth/permissions", "/swagger*", "/swagger-ui/*", "/api/v1/users/register", "/actuator/**",
			"/v3/api-docs/**", "/api/v1/login", "/api/v1/refresh" };

	/**
	 * 用於進行 URL 路徑匹配的 Ant 格式路徑匹配器。
	 */
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * 過濾器的核心方法，用於處理 Request 中的授權資訊。
	 * 
	 * @param request  HTTP 請求
	 * @param response HTTP 回應
	 * @param chain    過濾器鏈
	 * @throws ServletException 如果發生 Servlet 相關異常
	 * @throws IOException      如果發生 I/O 相關異常
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		log.debug("AuthenticationTokenFilter doFilterInternal");

		// 如果該 Request 不需要 JWT 驗證，則直接放行
		if (!requiresJwtValidation(request)) {
			chain.doFilter(request, response);
			return;
		}

		// 從 Request Header 中獲取授權資訊
		String authorization = request.getHeader("Authorization");

		if (authorization != null && authorization.startsWith("Bearer ")) {
			// 截取 Bearer 後面的 Access Token
			final String token = authorization.substring("Bearer ".length());
			ContextHolder.setJwtToken(token);
		}
		chain.doFilter(request, response);
		
	}

	/**
	 * 根據 Request 的 URL 判斷是否需要進行 JWT 驗證。
	 * 
	 * @param request HTTP 請求
	 * @return 是否需要進行 JWT 驗證
	 */
	private boolean requiresJwtValidation(HttpServletRequest request) {
		String requestPath = request.getRequestURI();
		// 檢查 Request 的 URL 是否在公開路徑列表中，如果是則不需要進行 JWT 驗證
		for (String publicPath : PUBLIC_PATHS) {
			log.info("publicPath:{}, requestPath:{}", publicPath, requestPath);

			if (pathMatcher.match(publicPath, requestPath)) {
				return false;
			}
		}

		return true;
	}

}
