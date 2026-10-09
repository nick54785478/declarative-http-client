package com.example.demo.iface.dto.res;

import com.example.demo.application.shared.dto.JwTokenGottenResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT Token 取得結果")
public record JwTokenGottenResource(
		@Schema(description = "回應代碼", example = "200") String code, 
		@Schema(description = "回應訊息", example = "Success") String message, 
		@Schema(description = "Token 資料內容") JwTokenGottenResult data) {
}
