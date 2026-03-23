package org.s4logic.payment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.s4logic.payment.dto.enums.Currency;
import org.s4logic.payment.dto.enums.PaymentMethod;
import org.s4logic.payment.dto.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Payment Entity
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@Entity(name = "PAYMENTS")
@Table(name = "PAYMENTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq_payments")
    @SequenceGenerator(name = "id_seq_payments", sequenceName = "PAYMENT_ID_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PAYMENT_INTENT_ID", unique = true)
    private String paymentIntentId;

    @Column(name = "CHECKOUT_SESSION_ID", unique = true)
    private String checkoutSessionId;

    @Column(name = "CLIENT_SECRET")
    private String clientSecret;

    @Column(name = "AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "CURRENCY", nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_METHOD")
    private PaymentMethod paymentMethod;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "ORDER_ID")
    private String orderId;

    @Column(name = "CUSTOMER_ID")
    private String customerId;

    @Column(name = "CUSTOMER_EMAIL")
    private String customerEmail;

    @Column(name = "PAYMENT_METHOD_ID")
    private String paymentMethodId;

    @Column(name = "RECEIPT_URL")
    private String receiptUrl;

    @Column(name = "REFUND_AMOUNT", precision = 19, scale = 2)
    private BigDecimal refundedAmount;

    @Column(name = "FAILURE_MESSAGE")
    private String failureMessage;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT", nullable = false)
    private OffsetDateTime updatedAt;
}
