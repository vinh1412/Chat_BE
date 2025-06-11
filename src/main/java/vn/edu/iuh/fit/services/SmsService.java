/**
 * @ (#) SmsService.java      4/10/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/10/2025
 */
public interface SmsService {
    public String generateOtp();
    public void sendOtp(String phoneNumber);

    public boolean verifyOtp(String phoneNumber, String otp);
}
