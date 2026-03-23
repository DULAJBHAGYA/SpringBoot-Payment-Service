package org.s4logic.payment.repository;

import org.s4logic.payment.dto.enums.PaymentStatus;
import org.s4logic.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Payment Repository
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    /**
     * Find Payment by Payment Intent ID
     *
     * @param paymentIntentId String
     * @return java.util.Optional<org.s4logic.payment.model.Payment>
     */
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    /**
     * Find Payment by Checkout Session ID
     *
     * @param checkoutSessionId String
     * @return java.util.Optional<org.s4logic.payment.model.Payment>
     */
    Optional<Payment> findByCheckoutSessionId(String checkoutSessionId);

    /**
     * Find Payments by Customer ID
     *
     * @param customerId String
     * @return @return java.util.List<org.s4logic.payment.model.Payment>
     */
    List<Payment> findByCustomerId(String customerId);

    /**
     * Find Payments by Order ID
     *
     * @param orderId String
     * @return java.util.List<org.s4logic.payment.model.Payment>
     */
    List<Payment> findByOrderId(String orderId);

    /**
     * Find Payments by Status
     *
     * @param status org.s4logic.payment.dto.enums.PaymentStatus
     * @return java.util.List<org.s4logic.payment.model.Payment>
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find Payments by Customer ID and Status
     *
     * @param customerId String
     * @param status     org.s4logic.payment.dto.enums.PaymentStatus
     * @return java.util.List<org.s4logic.payment.model.Payment>
     */
    List<Payment> findByCustomerIdAndStatus(String customerId, PaymentStatus status);
}
