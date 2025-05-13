package com.studydocs.notification.notification_service.service;

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

    /**
     * Gửi thông báo đến thiết bị sử dụng FCM.
     *
     * @param token   token của thiết bị cần nhận thông báo
     * @param title   tiêu đề thông báo
     * @param message nội dung thông báo
     * @throws Exception nếu có lỗi trong quá trình gửi thông báo
     */
    public void sendNotification(String token, String title, String message) throws Exception {
        // Tạo notification
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(message)
                .build();

        // Tạo message với FCM token của thiết bị
        Message fcmMessage = Message.builder()
                .setNotification(notification)
                .setToken(token)
                .build();

        // Gửi thông báo
        firebaseMessaging.send(fcmMessage);
    }
}
