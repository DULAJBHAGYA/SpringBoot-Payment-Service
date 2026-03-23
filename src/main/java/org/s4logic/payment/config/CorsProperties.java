package org.s4logic.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS Properties
 * <p>
 * Maps CORS-related properties from the application configuration file.
 *
 * @author Prasad Madusanka
 * @since 01 August 2024
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    private List<String> requestMatchers;
}
