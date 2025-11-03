package com.nckh.yte.security;

import com.nckh.yte.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filter JWT chạy 1 lần mỗi request, xác thực token và gán SecurityContextHolder.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String path = req.getServletPath();
        final String header = req.getHeader("Authorization");

        System.out.println("──────────────────────────────────────────────");
        System.out.println("[JwtAuthFilter] 🔍 Path: " + path);
        System.out.println("[JwtAuthFilter] 🔍 Authorization header: " + header);

        // ✅ Chỉ bỏ qua auth & swagger
        if (path.startsWith("/api/auth/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs")) {
            System.out.println("[JwtAuthFilter] ⚙️ Public endpoint → skip JWT check");
            chain.doFilter(req, res);
            return;
        }

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("[JwtAuthFilter] ⚠️ Không có header Bearer hợp lệ → skip");
            chain.doFilter(req, res);
            return;
        }

        final String token = header.substring(7).trim();

        try {
            boolean valid = jwtUtil.validate(token);
            System.out.println("[JwtAuthFilter] ✅ Token valid? " + valid);

            if (!valid) {
                System.err.println("[JwtAuthFilter] ❌ Token không hợp lệ hoặc hết hạn!");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                return;
            }

            final String username = jwtUtil.extractUsername(token);
            System.out.println("[JwtAuthFilter] 👤 Username extracted: " + username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("[JwtAuthFilter] 🧩 Authorities: " + userDetails.getAuthorities());

                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);

                System.out.println("[JwtAuthFilter] ✅ SecurityContextHolder set for user: " + username);
            }

        } catch (Exception e) {
            System.err.println("[JwtAuthFilter] ⚠️ Lỗi khi xác thực token: " + e.getMessage());
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Token processing failed\"}");
            return;
        }

        chain.doFilter(req, res);
    }
}
