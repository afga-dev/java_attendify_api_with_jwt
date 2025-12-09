package com.attendify.attendify_api.auth.service;

import com.attendify.attendify_api.auth.dto.AuthResponseDTO;
import com.attendify.attendify_api.auth.dto.ChangeEmailRequestDTO;
import com.attendify.attendify_api.auth.dto.ChangePasswordRequestDTO;
import com.attendify.attendify_api.auth.dto.LoginRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterAdminRequestDTO;
import com.attendify.attendify_api.auth.dto.RegisterRequestDTO;
import com.attendify.attendify_api.auth.enums.EmailResult;
import com.attendify.attendify_api.auth.enums.LogoutResult;
import com.attendify.attendify_api.auth.enums.PasswordResult;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO dto);

    AuthResponseDTO registerByAdmin(RegisterAdminRequestDTO dto);

    AuthResponseDTO login(LoginRequestDTO dto);

    AuthResponseDTO refreshToken(String header);

    LogoutResult logout(String header);

    PasswordResult changePassword(ChangePasswordRequestDTO dto);

    EmailResult changeEmail(ChangeEmailRequestDTO dto);
}
