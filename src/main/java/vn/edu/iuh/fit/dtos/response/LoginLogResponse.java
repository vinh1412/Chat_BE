/**
 * @ (#) LoginLogResponse.java      4/8/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.dtos.response;

import lombok.*;
import org.bson.types.ObjectId;
import vn.edu.iuh.fit.enums.LoginStatus;

import java.io.Serializable;
import java.time.Instant;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/8/2025
 */
@Getter
@Data
@Builder
@AllArgsConstructor @NoArgsConstructor
public class LoginLogResponse implements Serializable {
    private ObjectId userId;
    private Instant loginTime;
    private Instant logoutTime;

    private LoginStatus status;


}
