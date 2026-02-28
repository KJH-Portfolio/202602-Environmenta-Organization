package com.kh.spring.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kh.spring.util.JWTUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            // 2. 토큰 유효성 검증
            if (jwtUtil.validateToken(token)) {

                // 3. 토큰에서 사용자 정보(userId - 여기서는 LoginId) 추출
                String loginId = jwtUtil.getUserIdFromToken(token);

                if (loginId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 4. 인증 객체 생성
                    // 아이디만 있으면 됨. 비밀번호는 필요 없음(null). 권한은 비어있음.
                    Authentication auth = new UsernamePasswordAuthenticationToken(loginId, null,
                            Collections.emptyList());

                    // 5. SecurityContext에 저장 (인증 완료)
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("JWT Auth Success: {}", loginId);
                }
            } else {
                log.warn("Invalid JWT Token");
            }
        }

        filterChain.doFilter(request, response);
    }
}
