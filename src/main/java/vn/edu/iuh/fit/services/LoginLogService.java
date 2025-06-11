/**
 * @ (#) LoginLogService.java      4/8/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/8/2025
 */
public interface LoginLogService {
    void saveLogin(ObjectId userId);

    void saveLogout(ObjectId userId);

}
