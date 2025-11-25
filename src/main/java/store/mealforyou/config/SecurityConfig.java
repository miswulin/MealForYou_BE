package store.mealforyou.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import store.mealforyou.security.jwt.JwtAuthenticationFilter;
import store.mealforyou.security.jwt.JwtProvider;

@Configuration
@EnableWebSecurity // Spring Seucurity를 활성화하고 이 클래스의 @Bean들을 보안 체계에 등록해주는 어노테이션
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    // AuthenticationManager를 Bean으로 노출함으로써 AuthService에서 주입받아 로그인 시 직접 인증 흐름을 사용할 수 있게 함
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 스프링 시큐리티 필터 체인 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF: REST API + JWT 인증 구조에서는 보통 사용하지 않으므로 비활성화 (브라우저 폼 대신 토큰 사용)
                .csrf(csrf -> csrf.disable())
                // 폼 로그인/HTTP Basic은 사용하지 않음(JWT로만 인증)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션 사용 안 함: JWT 기반이므로 서버는 인증 상태를 세션에 저장하지 않음 (Stateless)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 401 인증실패
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, authEx) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"인증이 필요합니다.\"}");
                        })
                // 403 권한부족 (Access Denied)
                        .accessDeniedHandler((req, res, deniedEx) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"접근 권한이 없습니다.\"}");
                        })
                )


                // 경로별 인가(Authorization) 규칙
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 & 헬스체크 허용
                        .requestMatchers(
                                "/", "/index.html", "/health"
                        ).permitAll()

                        // 회원가입/로그인/토큰재발급/이메일 인증 관련 모두 허용
                        .requestMatchers("/api/auth/signup").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        .requestMatchers("/api/auth/email/**").permitAll()
                        .requestMatchers("/api/auth/password/reset").permitAll()

                        // /api/dis hes/ 로 시작하는 모든 경로는 인증 없이 허용
                        .requestMatchers("/api/dishes/**").permitAll()

                        // H2 콘솔 접근 허용 (개발용)
                        .requestMatchers("/h2-console/**").permitAll()

                        // 정적 리소스, 헬스체크
                        .requestMatchers("/", "/health").permitAll()

                        // Swagger / OpenAPI 문서 경로 허용
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()

                        // 로그아웃은 토큰이 있는 사용자만 호출 가능
                        .requestMatchers("/api/auth/logout").authenticated()
                        // 비밀번호 변경도 인증 필요
                        .requestMatchers("/api/auth/password/change").authenticated()
                        // 회원 탈퇴도 인증 필요
                        .requestMatchers("api/auth/delete").authenticated()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // H2 콘솔은 iframe을 사용하므로 X-Frame-Options 비활성화
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))

                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가 (폼 로그인용이므로 그 전에 JWT로 인증 세팅)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}