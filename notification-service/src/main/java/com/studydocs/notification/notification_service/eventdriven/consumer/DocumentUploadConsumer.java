package com.studydocs.notification.notification_service.eventdriven.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DocumentUploadConsumer {

    @KafkaListener(topics = "document-uploaded", groupId = "studydocs-notification-group")
    public void handleDocumentUploaded(String message) {
        System.out.println(message);
    }
}

