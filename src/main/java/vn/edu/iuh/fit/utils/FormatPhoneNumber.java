/**
 * @ (#) FormatPhoneNumber.java      4/7/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.utils;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/7/2025
 */
public class FormatPhoneNumber {
    public static String formatPhoneNumberTo84(String phoneNumber) {
        if (phoneNumber.startsWith("0")) {
            return "+84" + phoneNumber.substring(1);
        }
        if(phoneNumber.startsWith("+840")) {
            return "+84" + phoneNumber.substring(4);
        }
        if(phoneNumber.startsWith("84")) {

            return "+" + phoneNumber; // Nếu đã có +84 thì giữ nguyên
        }
        System.out.println("Phone number does not start with 0 or +840, returning original phone number: " + phoneNumber);

        return phoneNumber;
    }

    public static String formatPhoneNumberTo0(String phoneNumber) {
        if (phoneNumber.startsWith("+84")) {
            return "0" + phoneNumber.substring(3);
        }
        return phoneNumber; // Nếu đã có 0 thì giữ nguyên
    }
}
