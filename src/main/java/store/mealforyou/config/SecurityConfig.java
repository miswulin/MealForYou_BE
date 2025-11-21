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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 비활성화 (Postman 테스트 시 필수)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 요청에 대한 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // '/api/'로 시작하는 모든 요청은 인증 없이 허용 (permitAll)
                        .requestMatchers("/api/**").permitAll()
                        // 그 외의 요청도 일단 허용 (테스트 편의상) or .authenticated()로 막기
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}