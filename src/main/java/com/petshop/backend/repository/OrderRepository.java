package com.petshop.backend.repository;

import com.petshop.backend.entity.OrderEntity;
import com.petshop.backend.entity.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

	List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);

	List<OrderEntity> findByStatusOrderByCreatedAtDesc(OrderStatus status);
}
