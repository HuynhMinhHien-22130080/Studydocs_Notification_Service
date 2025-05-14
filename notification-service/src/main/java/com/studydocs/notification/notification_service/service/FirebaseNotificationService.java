package com.studydocs.notification.notification_service.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FirebaseNotificationService {
    private final FirebaseMessaging firebaseMessaging;

    public FirebaseNotificationService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public ApiFuture<String> sendNotification(String userId, String title, String message) {
        // Tạo notification
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(message)
                .build();

        // Tạo message với topic
        Message fcmMessage = Message.builder()
                .setNotification(notification)
                .setTopic(userId + "-notification")
                .build();
        return firebaseMessaging.sendAsync(fcmMessage);
    }
}
