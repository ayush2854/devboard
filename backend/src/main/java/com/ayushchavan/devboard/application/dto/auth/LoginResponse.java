package com.ayushchavan.devboard.application.dto.auth;

import com.ayushchavan.devboard.application.dto.user.UserResponse;

public record LoginResponse(

    String token,

    UserResponse user

) {
}
