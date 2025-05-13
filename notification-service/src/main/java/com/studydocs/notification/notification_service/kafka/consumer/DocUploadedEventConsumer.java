package com.studydocs.notification.notification_service.kafka.consumer;

import com.studydocs.notification.notification_service.event.EventConsumer;
import com.studydocs.notification.notification_service.model.event.DocumentUploadedEvent;
import com.studydocs.notification.notification_service.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DocUploadedEventConsumer implements EventConsumer<DocumentUploadedEvent> {

    private final NotificationService notificationService;

    public DocUploadedEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    @KafkaListener(topics = "document-uploaded", groupId = "studydocs-notification-group")
    public void consume(DocumentUploadedEvent event) {
        notificationService.notify(event);
    }
}

