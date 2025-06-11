/**
 * @ (#) LoginLog.java      4/7/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.edu.iuh.fit.enums.LoginStatus;

import java.time.Instant;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/7/2025
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "login_logging")
public class LoginLog {
    private ObjectId id;

    private ObjectId userId;

    private Instant loginTime;
    private Instant logoutTime;

    private LoginStatus status;
}
