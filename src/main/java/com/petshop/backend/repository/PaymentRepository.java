package com.petshop.backend.repository;

import com.petshop.backend.entity.Payment;
import com.petshop.backend.entity.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByProviderPaymentId(String providerPaymentId);

	Optional<Payment> findByOrderId(Long orderId);

	List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
}
