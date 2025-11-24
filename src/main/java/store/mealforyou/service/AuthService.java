package store.mealforyou.service;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.mealforyou.dto.LoginRequest;
import store.mealforyou.dto.RefreshRequest;
import store.mealforyou.dto.SignupRequest;
import store.mealforyou.dto.TokenResponse;
import store.mealforyou.entity.Member;
import store.mealforyou.repository.EmailAuthRepository;
import store.mealforyou.repository.MemberRepository;
import store.mealforyou.repository.RefreshTokenRepository;
import store.mealforyou.security.jwt.JwtProvider;
import store.mealforyou.util.PhoneNumberNormalizer;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션(SELECT) -> DB 쓰기가 필요한 메서드만 오버라이드로 명시
public class AuthService {

    // 의존성
    private final MemberRepository memberRepository; // JPA: 회원 데이터 영속화
    private final RefreshTokenRepository refreshTokenRepository; // Redis: Refresh 허용 리스트 저장
    private final PasswordEncoder passwordEncoder; // BCrypt 해시
    private final PhoneNumberNormalizer phoneNumberNormalizer; // 전화번호 E.164 변환
    private final JwtProvider jwtProvider; // JWT 발급/검증/파싱
    private final AuthenticationManager authenticationManager; // 시큐리티 표준 인증 엔진
    private final EmailAuthRepository emailAuthRepository;

    // 회원가입
    @Transactional
    public Long signup(SignupRequest dto) {

        String email = dto.email();

        // 중복 이메일 검사
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 이메일 인증 여부 확인
        if (!emailAuthRepository.isVerified(email)) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
        }

        // 비밀번호 재확인
        if (!dto.password().equals(dto.passwordConfirm())) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }

        // 전화번호 정규화 (문자열 파싱 -> E.164 국제 표준 문자열)
        final String phoneE164 = phoneNumberNormalizer.toE164(dto.phoneRaw(), "KR");

        // 주소 (null 가능)
        var address = dto.address() != null ? dto.address().toEmbeddable() : null;

        // 비밀번호 암호화
        Member member = Member.builder()
                .email(dto.email())
                .name(dto.name())
                .password(passwordEncoder.encode(dto.password()))
                .phoneE164(phoneE164)
                .address(address)
                .build();

        // 저장 후 기본키 반환
        return memberRepository.save(member).getId();
    }

    // 로그인 & 토큰 발급
    @Transactional
    public TokenResponse login(LoginRequest dto) {
        // 스프링 시큐리티 표준 인증 흐름:
        // UsernamePasswordAuthenticationToken: "자격 증명" 객체(아이디/비밀번호를 담는 자료구조)
        // AuthenticationManager.authenticate() 내부:
        // - DaoAuthenticationProvider → MemberDetailService.loadUserByUsername() → PasswordEncoder.matches(입력pw, 저장해시)까지 수행
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        // 인증 성공 -> JWT 발급
        String accessToken = jwtProvider.createAccessToken(dto.email());
        String refreshToken = jwtProvider.createRefreshToken(dto.email());

        // 서버 저장형 정책: RefreshToken을 Redis 허용 리스트에 보관
        refreshTokenRepository.save(dto.email(), refreshToken);

        // 클라이언트 계약: DTO로 명확한 응답 스키마 제공
        // - tokenType="Bearer": Authorization 헤더 생성을 위한 힌트 문자열
        // - expiresIn: Access 남은 시간(초) → 프론트 타이머/자동갱신 로직에 사용
        return new TokenResponse(accessToken, refreshToken, "Bearer", 60L*15);
    }

    // 리프레시(재발급)
    @Transactional
    public TokenResponse refresh(RefreshRequest request) {

        String clientRefreshToken = request.refreshToken();

        // refreshToken에서 email 추출
        String email;
        try {
            // 토큰이 진짜 JWT인지, 서명이 올바른지 검증 & 이메일 추출
            email = jwtProvider.getEmail(clientRefreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalStateException("유효하지 않은 리프레시 토큰입니다.");
        }


        // Redis에 저장된 refreshToken과 비교
        String saved = refreshTokenRepository.find(email);
        if (saved == null || !saved.equals(clientRefreshToken)) { // 만료/로그아웃일 경우
            throw new IllegalStateException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 새 JWT 발급
        String newAccess = jwtProvider.createAccessToken(email);
        String newRefresh = jwtProvider.createRefreshToken(email);

        // 교체: Redis의 "refresh:{email}" 값을 새 토큰으로 교체 & TTL 재설정
        refreshTokenRepository.save(email, newRefresh);

        return new TokenResponse(newAccess, newRefresh, "Bearer", 60L*15);
    }

    // 로그아웃
    @Transactional
    public void logout(String email, String refreshToken) {

        // 토큰에서 이메일 추출
        String tokenEmail = jwtProvider.getEmail(refreshToken);

        // Access 토큰에서 꺼낸 이메일과 Refresh 토큰 속 이메일이 다르면 위조 가능성이므로 예외처리
        if(!tokenEmail.equals(email)) {
            throw new IllegalStateException("토큰 정보가 일치하지 않습니다.");
        }

        // 허용리스트에서 삭제 -> 즉시 무효화 (문자열 키 제거)
        refreshTokenRepository.delete(email);
    }

    public String extractEmail(String refreshToken) {
        return jwtProvider.getEmail(refreshToken);
    }
}
