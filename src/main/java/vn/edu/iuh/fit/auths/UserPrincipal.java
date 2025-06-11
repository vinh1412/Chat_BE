/*
 * @ {#} UserPrincipal.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.auths;

import lombok.Data;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.iuh.fit.dtos.response.UserResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
@Data
public class UserPrincipal implements UserDetails {
    private final UserResponse userResponse;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (!userResponse.isEnabled()) {
            throw new DisabledException("User is disabled");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : userResponse.getRoles()) {
            authorities.add(new SimpleGrantedAuthority( role));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return userResponse.getPassword();
    }

    @Override
    public String getUsername() {
        return userResponse.getPhone();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
