/**
 * @ (#) MemberRoles.java      4/7/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.enums;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/7/2025
 */
public enum MemberRoles
{
    ADMIN("ADMIN"),
    DEPUTY("DEPUTY"),
    MEMBER("MEMBER");

    private String role;

    MemberRoles(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
