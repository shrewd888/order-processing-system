package com.orderprocessing.inventoryservice.job;

import com.orderprocessing.inventoryservice.repository.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ProcessedEventCleanup {

    @Autowired
    private ProcessedEventRepository repository;

    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2 AM
    public void cleanupOldEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        int deleted = repository.deleteByProcessedAtBefore(cutoff);
        log.info("🧹 Cleaned up {} processed events older than 30 days", deleted);
    }
}