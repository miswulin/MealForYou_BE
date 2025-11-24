package store.mealforyou.config;

import com.siot.IamportRestClient.IamportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortOneConfig {

    @Value("${portone.api.key}")
    private String apiKey;

    @Value("${portone.api.secret}")
    private String apiSecret;

    @Bean
    public IamportClient iamportClient() {
        // 해당 클라이언트가 포트원 서버와 통신을 담당
        return new IamportClient(apiKey, apiSecret);
    }
}