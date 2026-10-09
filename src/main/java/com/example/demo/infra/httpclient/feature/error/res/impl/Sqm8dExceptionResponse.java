package com.example.demo.infra.httpclient.feature.error.res.impl;

import com.example.demo.infra.httpclient.feature.error.res.ExceptionResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sqm8dExceptionResponse implements ExceptionResponse {

	private String code;

	private String message;

	private Integer status;

	private String path;
}