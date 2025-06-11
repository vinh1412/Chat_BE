/*
 * @ {#} JwtAccessTokenFilter.java   1.0     17/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.iuh.fit.auths.UserPrincipal;
import vn.edu.iuh.fit.services.RefreshTokenService;
import vn.edu.iuh.fit.services.impl.UserDetailsServiceImpl;
import vn.edu.iuh.fit.utils.JwtTokenUtil;

import java.io.IOException;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   17/03/2025
 * @version:    1.0
 */
@Component
public class JwtAccessTokenFilter extends OncePerRequestFilter {
    private JwtDecoder jwtDecoder;
    private JwtTokenUtil jwtTokenUtil;
    private UserDetailsServiceImpl userDetailsService;
    private RefreshTokenService refreshTokenService;

    public JwtAccessTokenFilter(JwtDecoder jwtDecoder, JwtTokenUtil jwtTokenUtil, UserDetailsServiceImpl userDetailsService, RefreshTokenService refreshTokenService) {
        this.jwtDecoder = jwtDecoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Jwt jwtToken = this.jwtDecoder.decode(token);
                String userName = jwtToken.getSubject();

                if (userName != null && !userName.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(userName);

                    // Kiểm tra nếu Refresh Token của user đã bị thu hồi
                    String refreshToken = refreshTokenService.getRefreshTokenByUser(userPrincipal.getUserResponse().getId());
                    if (refreshToken != null && refreshTokenService.isTokenRevoke(refreshToken)) {
                        SecurityContextHolder.clearContext();
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your session has expired. Please log in again.");
                        return;
                    }

                    if (jwtTokenUtil.isTokenValid(jwtToken, userPrincipal)) {
                        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                        UsernamePasswordAuthenticationToken createdToken = new UsernamePasswordAuthenticationToken(userPrincipal.getUsername(), userPrincipal.getPassword(), userPrincipal.getAuthorities());
                        createdToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        securityContext.setAuthentication(createdToken);
                        SecurityContextHolder.setContext(securityContext);
                    }
                }

            } catch (JwtException ex) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
