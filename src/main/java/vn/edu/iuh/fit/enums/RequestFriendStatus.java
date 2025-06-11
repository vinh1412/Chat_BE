/**
 * @ (#) RequestFriendStatus.java      4/7/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.enums;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/7/2025
 */
public enum RequestFriendStatus {
    PENDING("PENDING"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED");

    private final String status;

    RequestFriendStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
