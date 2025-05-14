package com.studydocs.notification.notification_service.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.studydocs.notification.notification_service.dao.DocumentDao;
import com.studydocs.notification.notification_service.model.entity.Documents;
import com.studydocs.notification.notification_service.model.event.DocumentUploadedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {
    private static final String NEW_DOCUMENT_NOTIFICATION_TITLE = "Bài viết mới";

    private final FirebaseNotificationService firebaseNotificationService;
    private final DocumentDao documentDao;


    public NotificationService(FirebaseNotificationService firebaseNotificationService, DocumentDao documentDao) {
        this.firebaseNotificationService = firebaseNotificationService;
        this.documentDao = documentDao;
    }

        @Async
        public void notify(DocumentUploadedEvent event) {
            try {
                Documents document = documentDao.getById(event.documentId());
                ApiFuture<String> future = firebaseNotificationService.sendNotification(
                        event.userId(),
                        NEW_DOCUMENT_NOTIFICATION_TITLE,
                        document.getDescription()
                );
                ApiFutures.addCallback(
                        future,
                        new ApiFutureCallback<>() {
                            @Override
                            public void onSuccess(String result) {
                                // Không cần xử lý khi thành công
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                // Xử lý lỗi khi gửi notification
                            }
                        },
                        Runnable::run
                );
            } catch (Exception e) {
            }
        }


}

