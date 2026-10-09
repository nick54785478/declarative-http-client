package com.example.demo.application.service;

import com.example.demo.application.port.in.AuthUseCase;
import com.example.demo.application.port.out.AuthServiceClientPort;
import com.example.demo.application.shared.command.inbound.GetJwTokenCommand;
import com.example.demo.application.shared.command.outbound.GetJwTokenFromAuthServiceCommand;
import com.example.demo.application.shared.dto.JwTokenGottenFromAuthServiceData;
import com.example.demo.application.shared.dto.JwTokenGottenResult;
import com.example.demo.application.shared.dto.UserInfoGottenFromAuthServiceData;
import com.example.demo.application.shared.dto.UserInfoGottenResult;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthUseCase {

    private final AuthServiceClientPort authSystemPort;

    public AuthService(AuthServiceClientPort authSystemPort) {
        this.authSystemPort = authSystemPort;
    }

    @Override
    public JwTokenGottenResult login(GetJwTokenCommand command) {
        GetJwTokenFromAuthServiceCommand getJwTokenFromAuthServiceCommand = new GetJwTokenFromAuthServiceCommand(command.getTenant(),
                command.getUsername(), command.getPassword());
        JwTokenGottenFromAuthServiceData responseData = authSystemPort.login(getJwTokenFromAuthServiceCommand);
        return new JwTokenGottenResult(responseData.getToken());
    }

    @Override
    public UserInfoGottenResult getUserById(Long id) {
        UserInfoGottenFromAuthServiceData user = authSystemPort.getUserById(id);
        return new UserInfoGottenResult(user.getId(), user.getName(),
                user.getEmail(), user.getAddress());
    }

}
