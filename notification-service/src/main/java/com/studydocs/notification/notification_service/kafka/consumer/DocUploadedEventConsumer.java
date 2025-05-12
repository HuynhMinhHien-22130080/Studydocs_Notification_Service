package com.studydocs.notification.notification_service.kafka.consumer;

import com.studydocs.notification.notification_service.dao.DeviceTokenDao;
import com.studydocs.notification.notification_service.event.EventConsumer;
import com.studydocs.notification.notification_service.model.event.DocumentUploadedEvent;
import com.studydocs.notification.notification_service.service.FirebaseNotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DocUploadedEventConsumer implements EventConsumer<DocumentUploadedEvent> {

    private final FirebaseNotificationService firebaseNotificationService;
    private final DeviceTokenDao deviceTokenDao;

    public DocUploadedEventConsumer(FirebaseNotificationService firebaseNotificationService, DeviceTokenDao deviceTokenDao) {
        this.firebaseNotificationService = firebaseNotificationService;
        this.deviceTokenDao = deviceTokenDao;
    }

    @Override
    @KafkaListener(topics = "document-uploaded", groupId = "studydocs-notification-group")
    public void consume(DocumentUploadedEvent event) {

        String deviceToken = "device_token_here"; // Bạn cần lấy token từ nơi lưu trữ

        String title = "Tài liệu mới đã được tải lên";
        String message = "Tài liệu " + event.documentId() + " đã được tải lên bởi người dùng " + event.userId();

        try {
            firebaseNotificationService.sendNotification(deviceToken, title, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

