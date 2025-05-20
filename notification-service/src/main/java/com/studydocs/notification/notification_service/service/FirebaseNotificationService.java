package com.studydocs.notification.notification_service.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FirebaseNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseNotificationService.class);
    private final FirebaseMessaging firebaseMessaging;

    public FirebaseNotificationService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public void sendNotification(String fcmToken, String title, String message) throws FirebaseMessagingException {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(message)
                    .build();

            Message fcmMessage = Message.builder()
                    .setNotification(notification)
                    .setToken(fcmToken)
                    .build();
                    
            firebaseMessaging.send(fcmMessage);
        } catch (FirebaseMessagingException e) {
            logger.error("Lỗi gửi FCM: token={}, error={}", fcmToken, e.getMessage());
            throw e;
        }
    }
}
