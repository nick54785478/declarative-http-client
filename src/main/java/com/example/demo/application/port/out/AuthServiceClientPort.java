package com.example.demo.application.port.out;

import com.example.demo.application.shared.command.outbound.GetJwTokenFromAuthServiceCommand;
import com.example.demo.application.shared.dto.JwTokenGottenFromAuthServiceData;
import com.example.demo.application.shared.dto.UserInfoGottenFromAuthServiceData;

/**
 * Auth 系統的 Outbound Port (輸出埠)。
 *
 * <p>
 * 遵循 Clean Architecture 設計，此介面定義了 Application 層所需呼叫 Auth 系統的業務合約。
 * Infra 層的 Adapter 必須實作此介面來處理具體的連線邏輯（例如 HTTP）。
 * 如此一來，Application 層便能與具體的技術實作解耦。
 * </p>
 */
public interface AuthServiceClientPort {

	/**
	 * 進行使用者登入，並取得 JWT Token。
	 *
	 * @param command 包含租戶、帳號與密碼的指令物件
	 * @return 包含 Token 的結果資料
	 */
	JwTokenGottenFromAuthServiceData login(GetJwTokenFromAuthServiceCommand command);

	/**
	 * 透過使用者 ID 取得詳細的使用者基本資料。
	 *
	 * @param id 欲查詢的使用者 ID
	 * @return 該使用者的基本資料
	 */
	UserInfoGottenFromAuthServiceData getUserById(Long id);

}
