package com.orderprocessing.inventoryservice.repository;

import com.orderprocessing.inventoryservice.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String>
{
    @Modifying
    @Transactional
    int deleteByProcessedAtBefore(LocalDateTime cutoff);
}