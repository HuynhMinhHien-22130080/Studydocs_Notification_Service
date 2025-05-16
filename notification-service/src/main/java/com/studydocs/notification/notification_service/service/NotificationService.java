package com.studydocs.notification.notification_service.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.studydocs.notification.notification_service.dao.DocumentDao;
import com.studydocs.notification.notification_service.dao.UserDao;
import com.studydocs.notification.notification_service.model.entity.Documents;
import com.studydocs.notification.notification_service.model.event.DocumentUploadedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;


@Service
public class NotificationService {
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
        try {
            Documents document = documentDao.getById(event.documentId());
            Map<String, String[]> fcmTokens = userDao.getFCMTokensForNotifiableFollowers(event.userId());
            for (Map.Entry<String, String[]> entry : fcmTokens.entrySet()) {
                for (String token : entry.getValue()) {
                    try {
                        firebaseNotificationService.sendNotification(token, NEW_DOCUMENT_NOTIFICATION_TITLE, document.getDescription());
                    } catch (FirebaseMessagingException e) {
                        MessagingErrorCode errorCode = e.getMessagingErrorCode();
                        if (errorCode == MessagingErrorCode.UNREGISTERED) {
                            userDao.removeFCMToken(entry.getKey(), token);
                        }
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
        }
    }


}

