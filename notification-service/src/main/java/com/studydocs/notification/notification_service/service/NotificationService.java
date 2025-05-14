package com.studydocs.notification.notification_service.service;

import com.studydocs.notification.notification_service.dao.DocumentDao;
import com.studydocs.notification.notification_service.dao.UserDao;
import com.studydocs.notification.notification_service.model.entity.DeviceToken;
import com.studydocs.notification.notification_service.model.entity.Documents;
import com.studydocs.notification.notification_service.model.entity.Followers;
import com.studydocs.notification.notification_service.model.event.DocumentUploadedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;


@Service
public class NotificationService {
    private static final String NEW_DOCUMENT_NOTIFICATION_TITLE = "Bài viết mới";

    private final FirebaseNotificationService firebaseNotificationService;
    private final UserDao userDao;
    private final DocumentDao documentDao;

    private final int activeMonthsThreshold;

    public NotificationService(
            FirebaseNotificationService firebaseNotificationService,
            UserDao userDao,
            DocumentDao documentDao,
            @Value("${notification.device-token.active-months}") int activeMonthsThreshold
    ) {
        this.firebaseNotificationService = firebaseNotificationService;
        this.userDao = userDao;
        this.documentDao = documentDao;
        this.activeMonthsThreshold = activeMonthsThreshold;
    }

    public void notify(DocumentUploadedEvent event) {
        try {
            List<Followers> followers = userDao.getFollowers(event.userId());

            List<String> activeDeviceTokens = getActiveTokensForFollowers(followers);
            Documents document = documentDao.getById(event.documentId());

            notifyDevices(activeDeviceTokens, document.getDescription());
        } catch (Exception e) {
        }
    }

    private List<String> getActiveTokensForFollowers(List<Followers> followers) {
        return followers.stream()
                .flatMap(follower -> {
                    try {
                        return getActiveDeviceTokens(userDao.getDeviceTokens(follower.getUserId())).stream();
                    } catch (ExecutionException | InterruptedException e) {
                        return List.<String>of().stream();
                    }
                })
                .toList();
    }

    private void notifyDevices(List<String> deviceTokens, String message) {
        for (String token : deviceTokens) {
            try {
                firebaseNotificationService.sendNotification(token, NEW_DOCUMENT_NOTIFICATION_TITLE, message);
            } catch (Exception e) {
            }
        }
    }

    private List<String> getActiveDeviceTokens(List<DeviceToken> deviceTokens) {
        return deviceTokens.stream()
                .filter(token -> token.isActiveWithinMonths(activeMonthsThreshold))
                .map(DeviceToken::getToken)
                .toList();
    }
}

