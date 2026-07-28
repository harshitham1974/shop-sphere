package com.ecom.shopsphere.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.shopsphere.entity.Category;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    boolean existsByCategoryNameIgnoreCase(String categoryName);

    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
}