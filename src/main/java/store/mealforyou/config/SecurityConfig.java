package store.mealforyou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF, Form Login, Http Basic 인증 비활성화 (API 서버이므로)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 2. 요청별 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // /api/dishes/ 로 시작하는 모든 경로는 인증 없이 허용
                        .requestMatchers("/api/dishes/**").permitAll()

                        // H2 콘솔 접근 허용 (개발용)
                        .requestMatchers("/h2-console/**").permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // H2 콘솔은 iframe을 사용하므로 X-Frame-Options 비활성화
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }
}