package com.example.demo.iface.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "取得 JWT Token 的請求參數")
public class GetJwTokenResource {

	@Schema(description = "租戶名稱", example = "CW")
	private String tenant;
	
	@Schema(description = "使用者帳號", example = "nickgh.zhang@cw.com")
	private String username;

	@Schema(description = "密碼", example = "password123")
	private String password;
}
