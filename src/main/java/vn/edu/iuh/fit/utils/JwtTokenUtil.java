/*
 * @ {#} JwtTokenUtil.java   1.0     17/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.auths.UserPrincipal;

import java.time.Instant;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   17/03/2025
 * @version:    1.0
 */
@Component
public class JwtTokenUtil {
    private static final Logger LOGGER = Logger.getLogger(JwtTokenUtil.class.getName());
    @SuppressWarnings("ReassignedVariable")
    public String generateToken(Authentication authentication, JwtEncoder jwtEncoder) {
        String token = "";
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("edu.iuh.fit")
                    .issuedAt(now)
                    .expiresAt(generateExpirationDate())
                    .subject(userPrincipal.getUsername())
                    .claim("roles", userPrincipal.getAuthorities()
                            .stream()
                            .map(r -> r.getAuthority().replace("SCOPE_", ""))
                            .collect(Collectors.toList()))
                    .build();
            token=jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
        return token;
    }
    public String generateRefreshToken(Authentication authentication, JwtEncoder jwtEncoder) {
        String token = "";
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("edu.iuh.fit")
                    .issuedAt(now)
                    .expiresAt(generateRefreshExpirationDate()) // 🔥 Refresh Token có thời gian sống dài hơn
                    .subject(userPrincipal.getUsername())
                    .build(); // Không cần claim "roles" cho Refresh Token
            token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
        return token;
    }

    public String getPhoneFromToken(Jwt jwtToken) {
        return jwtToken.getSubject();
    }
    public boolean isTokenExpired(Jwt jwtToken) {
        return Objects.requireNonNull(jwtToken.getExpiresAt()).isBefore(Instant.now());
    }
    public boolean isTokenValid(Jwt jwtToken, UserPrincipal userPrincipal) {
        return !isTokenExpired(jwtToken) &&
                userPrincipal.isEnabled() &&
                userPrincipal.getUsername().equals(getPhoneFromToken(jwtToken));
    }
    public Instant generateExpirationDate() {
        return Instant.now().plus(10, java.time.temporal.ChronoUnit.MINUTES);
    }
    public Instant generateRefreshExpirationDate() {
        return Instant.now().plus(7, java.time.temporal.ChronoUnit.DAYS);
    }

}
