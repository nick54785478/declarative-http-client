package com.example.demo.application.port.in;

import com.example.demo.application.shared.command.inbound.GetJwTokenCommand;
import com.example.demo.application.shared.dto.JwTokenGottenResult;
import com.example.demo.application.shared.dto.UserInfoGottenResult;

/**
 * Auth 系統的 Inbound Port (輸入埠) / UseCase (應用服務)。
 *
 * <p>
 * 遵循 Clean Architecture 設計，此 UseCase 定義了 Application 層提供給外部 (如 Controller) 呼叫的業務場景。
 * 註：此 UseCase (或稱 Application Service) 目的為向外部微服務 AuthService 取得相關資料。
 * </p>
 */
public interface AuthUseCase {
    
	/**
	 * 進行使用者登入，並取得 JWT Token。
	 *
	 * @param command 包含租戶、帳號與密碼的指令物件
	 * @return 包含 Token 的結果資料
	 */
    JwTokenGottenResult login(GetJwTokenCommand command);

	/**
	 * 透過使用者 ID 取得詳細的使用者基本資料。
	 *
	 * @param id 欲查詢的使用者 ID
	 * @return 該使用者的基本資料
	 */
    UserInfoGottenResult getUserById(Long id);

}
