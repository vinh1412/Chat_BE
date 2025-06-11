/*
 * @ {#} LoginRequest.java   1.0     15/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.request;

import lombok.Builder;
import lombok.Data;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   15/03/2025
 * @version:    1.0
 */
@Data
@Builder
public class SignInRequest {
    private String phone;
    private String password;
}
