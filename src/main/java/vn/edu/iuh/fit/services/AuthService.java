/*
 * @ {#} AuthServer.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.dtos.request.SignInRequest;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.dtos.response.RefreshTokenResponse;
import vn.edu.iuh.fit.dtos.response.SignInResponse;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
public interface AuthService {
    boolean signUp(SignUpRequest signUpRequest);
    SignInResponse signIn(SignInRequest signInRequest);
    void logout(String token);
    RefreshTokenResponse refreshToken(String refreshToken);

}
