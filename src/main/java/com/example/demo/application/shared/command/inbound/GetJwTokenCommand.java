package com.example.demo.application.shared.command.inbound;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetJwTokenCommand {

	private String tenant;

	private String username;
	
	private String password;
}
