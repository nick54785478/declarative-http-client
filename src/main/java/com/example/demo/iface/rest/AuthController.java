package com.example.demo.iface.rest;

import com.example.demo.application.port.in.AuthUseCase;
import com.example.demo.application.shared.command.inbound.GetJwTokenCommand;
import com.example.demo.application.shared.dto.JwTokenGottenResult;
import com.example.demo.application.shared.dto.UserInfoGottenFromAuthServiceData;
import com.example.demo.application.shared.dto.UserInfoGottenResult;
import com.example.demo.iface.dto.req.GetJwTokenResource;
import com.example.demo.iface.dto.res.JwTokenGottenResource;
import com.example.demo.iface.dto.res.UserGottenResource;
import com.example.demo.infra.util.BaseDataTransformer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth API", description = "使用者驗證與資料查詢介面")
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @Operation(summary = "使用者登入", description = "使用帳號密碼向 AuthService 登入，成功將回傳 JWT Token")
    @PostMapping("/login")
    public ResponseEntity<JwTokenGottenResource> getJwToken(@RequestBody GetJwTokenResource resource) {
        GetJwTokenCommand command = BaseDataTransformer.transformData(resource, GetJwTokenCommand.class);
        JwTokenGottenResult token = authUseCase.login(command);
        return new ResponseEntity<>(new JwTokenGottenResource("200", "Success", token), HttpStatus.OK);
    }

    @Operation(summary = "取得使用者資訊", description = "透過使用者 ID 向 AuthService 查詢詳細基本資料")
    @GetMapping("/user/{id}")
    public ResponseEntity<UserGottenResource> getUserById(@PathVariable Long id) {
        UserInfoGottenResult data = authUseCase.getUserById(id);
        return new ResponseEntity<>(new UserGottenResource("200", "Success", data), HttpStatus.OK);
    }
}
