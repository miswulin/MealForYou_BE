package store.mealforyou.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Swagger / OpenAPI 문서의 기본 정보 설정용 설정 클래스
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mealforyouOpenAPI() {
        // 스웨거에서 참조할 Security Scheme 이름
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // 이 API는 bearerAuth라는 보안 스키마를 사용한다고 전체에 선언
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // 실제 Security Scheme 정의 (헤더에 Authorization: Bearer xxx 형태로 보내도록 설정)
                .components(new Components()
                        .addSecuritySchemes(
                                securitySchemeName,
                                new SecurityScheme()
                                        .name("Authorization")                 // 헤더 이름
                                        .type(SecurityScheme.Type.HTTP)       // HTTP auth 타입
                                        .scheme("bearer")                     // Bearer 스킴
                                        .bearerFormat("JWT")                  // 형식 설명 (옵션)
                                        .in(SecurityScheme.In.HEADER)         // 헤더에 넣는다는 의미
                        )
                )
                // 문서 기본 정보
                .info(new Info()
                        .title("MealForYou API")
                        .description("백엔드 API 문서입니다.")
                        .version("v1.0")
                )
                // 서버 정보
                .servers(List.of(
                        new Server()
                                .url("https://mealforyou.store")
                                .description("Production Server"),
                        new Server()
                                .url("https://www.mealforyou.store")
                                .description("Production Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ));
    }
}
