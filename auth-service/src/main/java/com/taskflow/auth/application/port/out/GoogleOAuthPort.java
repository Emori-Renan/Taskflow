package com.taskflow.auth.application.port.out;

import com.taskflow.auth.application.dto.GoogleUserInfoDTO;

public interface GoogleOAuthPort {
    String getAuthorizationUrl();
    GoogleUserInfoDTO exchangeCodeForUserInfo(String code);
}
