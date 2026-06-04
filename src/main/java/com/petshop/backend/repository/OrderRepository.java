package com.petshop.backend.repository;

import com.petshop.backend.entity.OrderEntity;
import com.petshop.backend.entity.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

	List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<OrderEntity> findByStatusOrderByCreatedAtDesc(OrderStatus status);
}
