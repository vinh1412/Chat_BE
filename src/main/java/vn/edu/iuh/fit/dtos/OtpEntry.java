/**
 * @ (#) OtpEntry.java      4/10/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/10/2025
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OtpEntry {
    private String otp;
    private long timestamp;
}
