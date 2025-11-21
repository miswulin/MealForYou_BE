package store.mealforyou.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.phone")
public record PhoneProps(String defaultRegion) {
}
