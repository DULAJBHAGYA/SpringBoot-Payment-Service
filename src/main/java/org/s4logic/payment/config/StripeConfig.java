package org.s4logic.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe Configuration
 * <p>
 * Maps Stripe-related properties from the application configuration file
 * and initializes the Stripe API key.
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@Configuration
@ConfigurationProperties(prefix = "stripe")
@Getter
@Setter
public class StripeConfig {
    private String apiKey;
    private String publicKey;

    /**
     * Initialize Stripe API key after properties are set
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = this.apiKey;
    }
}
