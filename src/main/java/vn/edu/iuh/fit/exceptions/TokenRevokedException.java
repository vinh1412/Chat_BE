/*
 * @ {#} TokenRevokedException.java   1.0     09/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   09/04/2025
 * @version:    1.0
 */
public class TokenRevokedException extends RuntimeException {
    public TokenRevokedException(String message) {
        super(message);
    }
}
