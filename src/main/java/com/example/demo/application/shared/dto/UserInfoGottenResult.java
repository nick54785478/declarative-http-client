package com.example.demo.application.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoGottenResult {

	private Long id;

	private String name;

	private String email;

	private String address;

}
