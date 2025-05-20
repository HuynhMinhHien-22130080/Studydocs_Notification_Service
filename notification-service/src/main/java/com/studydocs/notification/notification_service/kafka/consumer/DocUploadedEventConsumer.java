package com.studydocs.notification.notification_service.kafka.consumer;

import com.studydocs.notification.notification_service.event.EventConsumer;
import com.studydocs.notification.notification_service.model.event.DocumentUploadedEvent;
import com.studydocs.notification.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DocUploadedEventConsumer implements EventConsumer<DocumentUploadedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DocUploadedEventConsumer.class);
    private final NotificationService notificationService;

    public DocUploadedEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    @KafkaListener(topics = "document-uploaded", groupId = "studydocs-notification-group")
    public void consume(DocumentUploadedEvent event) {
        logger.info("Nhận event document-uploaded: documentId={}, userId={}", event.documentId(), event.userId());
        try {
            notificationService.notify(event);
            logger.info("Đã xử lý xong event document-uploaded: documentId={}", event.documentId());
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý event document-uploaded: documentId={}, error={}", event.documentId(), e.getMessage(), e);
            throw e;
        }
    }
}

