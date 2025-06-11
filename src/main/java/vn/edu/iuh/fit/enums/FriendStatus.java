/**
 * @ (#) FriendStatus.java      4/7/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.enums;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/7/2025
 */
public enum FriendStatus {
    ACTIVE("ACTIVE"), BLOCKED("BLOCKED");

    private final String value;

    private FriendStatus(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
