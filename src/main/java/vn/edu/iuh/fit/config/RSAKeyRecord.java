/*
 * @ {#} RSAKeyRecord.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
@ConfigurationProperties(prefix = "rsa")
public record RSAKeyRecord (RSAPublicKey rsaPublicKey, RSAPrivateKey rsaPrivateKey) { }
