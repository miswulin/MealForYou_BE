package store.mealforyou.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, // 요청 객체
            HttpServletResponse response, // 응답 객체
            FilterChain filterChain // 다음 필터들의 연쇄
    ) throws ServletException, IOException {
        // 요청 헤더에서 Access Token 추출
        String token = resolveToken(request);

        // 토큰이 있고, 그게 유효한 토큰이지만 아직 인증이 안된 경우
        if (token != null && jwtProvider.validate(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 토큰으로 Authentication 생성 (사용자 정보 + 권한 포함)
            Authentication authentication = jwtProvider.getAuthentication(token);

            // 현재 요청 스레드의 SecurityContext에 Authentication 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 다음 필터(또는 DispatcherServlet)/컨트롤러로 요청 넘김
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 "Bearer <token>" 형식의 토큰만 추출하는 메서드
    // - 토큰이 없거나 형식이 다르면 null 반환
    private String resolveToken(HttpServletRequest request) {
        // "Authorization" 헤더 값 조회
        String bearerToken = request.getHeader("Authorization");

        // StringUtils.hasText(): null/빈 문자열/공백만이 아닌 문자열인지 검사
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 접두사 7글자 제거 후 순수 토큰 부분만 반환
            return bearerToken.substring(7);
        }
        return null;
    }
}
