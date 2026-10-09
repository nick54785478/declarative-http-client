package com.example.demo.application.shared.command.outbound;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetJwTokenFromAuthServiceCommand {

	private String tenant; // 租戶

	private String username; // 使用者名稱

	private String password; // 密碼
}
