package com.example.demo.iface.dto.res;

import com.example.demo.application.shared.dto.UserInfoGottenResult;
import io.swagger.v3.oas.annotations.media.Schema;
import com.example.demo.application.shared.dto.UserInfoGottenFromAuthServiceData;

@Schema(description = "使用者詳細資訊取得結果")
public record UserGottenResource(
		@Schema(description = "回應代碼", example = "200") String code, 
		@Schema(description = "回應訊息", example = "Success") String message, 
		@Schema(description = "使用者資料內容") UserInfoGottenResult data) {
}
