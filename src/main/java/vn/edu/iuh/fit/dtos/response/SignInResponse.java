/*
 * @ {#} SignInResponse.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignInResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String id;
    private String phone;
    private Collection<? extends GrantedAuthority> roles;
}
