/**
 * @ (#) MessageType.java      4/7/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.enums;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/7/2025
 */
public enum MessageType {
    TEXT("TEXT"),
    IMAGE("IMAGE"),
    VIDEO("VIDEO"),
    AUDIO("AUDIO"),
    FILE("FILE"),
    STICKER("STICKER"),
    GIF("GIF"),
    EMOJI("EMOJI"),
    SYSTEM("SYSTEM"),
    POLL("POLL"),;

    private String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
