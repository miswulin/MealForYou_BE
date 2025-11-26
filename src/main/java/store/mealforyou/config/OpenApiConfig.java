package store.mealforyou.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Swagger / OpenAPI 문서의 기본 정보 설정용 설정 클래스
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mealforyouOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MealForYou API")
                        .description("백엔드 API 문서입니다.")
                        .version("v1.0")
                )
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
