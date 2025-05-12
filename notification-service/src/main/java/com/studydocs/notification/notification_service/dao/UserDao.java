package com.studydocs.notification.notification_service.dao;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.studydocs.notification.notification_service.model.entity.DeviceToken;
import com.studydocs.notification.notification_service.model.entity.Followers;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class UserDao {
    private final Firestore firestore;
    private static final String USER_COLLECTION = "users";
    private static final String DEVICE_COLLECTION = "deviceTokens";
    private static final String FOLLOWERS_COLLECTION = "followers";

    public UserDao(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Lấy DocumentReference của user theo userId
     */
    public DocumentReference getDocumentById(String userId) {
        return firestore.collection(USER_COLLECTION).document(userId);
    }

    /**
     * Lấy danh sách DeviceToken của user
     */
    public List<DeviceToken> getDeviceTokens(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = getDocumentById(userId)
                .collection(DEVICE_COLLECTION)
                .get()
                .get();

        return snapshot.getDocuments()
                .stream()
                .map(doc -> doc.toObject(DeviceToken.class))
                .toList();
    }

    /**
     * Lấy danh sách follower theo userId
     */
    public List<Followers> getFollowers(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = getDocumentById(userId)
                .collection(FOLLOWERS_COLLECTION)
                .get()
                .get();
        return snapshot.getDocuments()
                .stream()
                .map(doc -> doc.toObject(Followers.class))
                .toList();
    }
}
