package com.example.toss.repo;

import com.example.toss.entity.ItemOrder;
import org.springframework.data.jpa.repository.JpaRepository;

// 주문 정보를 저장하기 위한 Repository
public interface OrderRepository extends JpaRepository<ItemOrder, Long> {}
