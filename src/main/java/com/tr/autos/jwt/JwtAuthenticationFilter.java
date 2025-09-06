package com.tr.autos.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var claims = jwtTokenProvider.parse(token).getBody();

                Object typ = claims.get("typ");
                if (!"access".equals(typ)) {
                    // access 토큰이 아니면 인증 세팅하지 않고 다음 필터로 넘김
                    fc.doFilter(req, res);
                    return;
                }

                String email = claims.getSubject();
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                // 토큰 불일치/만료 → 401
                SecurityContextHolder.clearContext();
                //res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "만료된 토큰입니다."); // 제거
                req.setAttribute("authError", "만료되었거나 유효하지 않은 토큰입니다."); // 이유 전달
                // 인증 없이 진행 -> Security가 보호 자원에서 EntryPoint를 호출해 401 JSON 응답
            }
        }
        fc.doFilter(req, res);
    }
}
