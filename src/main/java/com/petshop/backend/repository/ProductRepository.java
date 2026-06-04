package com.petshop.backend.repository;

import com.petshop.backend.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findAllByOrderByNameAsc();

	List<Product> findByActiveTrueOrderByNameAsc();

	List<Product> findByCategoryIdAndActiveTrueOrderByNameAsc(Long categoryId);

	Optional<Product> findByIdAndActiveTrue(Long id);

	boolean existsByCategoryId(Long categoryId);
}
