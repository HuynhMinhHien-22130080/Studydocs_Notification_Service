package com.studydocs.notification.notification_service.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.studydocs.notification.notification_service.dao.DocumentDao;
import com.studydocs.notification.notification_service.dao.UserDao;
import com.studydocs.notification.notification_service.model.entity.Documents;
import com.studydocs.notification.notification_service.model.entity.Notifications;
import com.studydocs.notification.notification_service.model.event.DocumentUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final String NEW_DOCUMENT_NOTIFICATION_TITLE = "Bài viết mới";

    private final FirebaseNotificationService firebaseNotificationService;
    private final UserDao userDao;
    private final DocumentDao documentDao;


    public NotificationService(FirebaseNotificationService firebaseNotificationService, UserDao userDao, DocumentDao documentDao) {
        this.firebaseNotificationService = firebaseNotificationService;
        this.userDao = userDao;
        this.documentDao = documentDao;
    }

    @Async
    public void notify(DocumentUploadedEvent event) {
        logger.info("Xử lý thông báo: docId={}, userId={}", event.documentId(), event.userId());
        try {
            Documents document = documentDao.getById(event.documentId());
            Map<String, String[]> fcmTokens = userDao.getFCMTokensForNotifiableFollowers(event.userId());
            
            if (fcmTokens.isEmpty()) {
                logger.info("Không có follower nào cần thông báo cho docId={}", event.documentId());
                return;
            }
            
            logger.info("Gửi thông báo cho {} follower của docId={}", fcmTokens.size(), event.documentId());
            
            for (Map.Entry<String, String[]> entry : fcmTokens.entrySet()) {

                String followerId = entry.getKey();
                String[] tokens = entry.getValue();
                

                //Tạo notification
                Notifications notifications = Notifications.builder()
                        .senderId(event.userId())
                        .documentId(event.documentId())
                        .type("new_document")
                        .title(NEW_DOCUMENT_NOTIFICATION_TITLE)
                        .message(document.getDescription())
                        .build();
              
                userDao.addNotification(followerId, notifications);
                
                //Gửi FCM Token
                int successCount = 0;
                int invalidTokenCount = 0;
                
                for (String token : tokens) {
                    try {
                        firebaseNotificationService.sendNotification(token, NEW_DOCUMENT_NOTIFICATION_TITLE, notifications.getMessage());
                        successCount++;
                    } catch (FirebaseMessagingException e) {
                        MessagingErrorCode errorCode = e.getMessagingErrorCode();
                        if (errorCode == MessagingErrorCode.UNREGISTERED) {
                            userDao.removeFCMToken(followerId, token);
                            invalidTokenCount++;
                        }
                    }
                }
                
                if (invalidTokenCount > 0) {
                    logger.warn("Follower {} có {} token không hợp lệ đã bị xóa", followerId, invalidTokenCount);
                }
            }
            
            logger.info("Hoàn thành gửi thông báo cho docId={}", event.documentId());
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Lỗi xử lý thông báo docId={}: {}", event.documentId(), e.getMessage());
        }
    }


}

