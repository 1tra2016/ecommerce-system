package com.example.orderinventoryservice.repository;

import com.example.orderinventoryservice.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxEventRepository
        extends JpaRepository<InboxEvent, String> {
}