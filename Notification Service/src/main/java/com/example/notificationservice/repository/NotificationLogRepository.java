package com.example.notificationservice.repository;

import com.example.notificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationLogRepository extends
        JpaRepository<NotificationLog, Long>,
        JpaSpecificationExecutor<NotificationLog> {
}
