package store.mealforyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import store.mealforyou.util.PhoneProps;

@EnableConfigurationProperties(PhoneProps.class)
@SpringBootApplication
public class MealForYouApplication {
    public static void main(String[] args) {
        SpringApplication.run(MealForYouApplication.class, args);
    }
}