package com.studydocs.notification.notification_service.dao;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.studydocs.notification.notification_service.model.entity.Follower;
import com.studydocs.notification.notification_service.model.entity.Notifications;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class UserDao {
    private static final String USERS_COLLECTION = "users"; // Tên collection chính chứa user
    private static final String FOLLOWERS_COLLECTION = "followers"; // Tên subcollection chứa danh sách follower dạng sharded
    private static final String FOLLOWING_COLLECTION_PREFIX = "following_"; // Prefix cho subcollection following (theo từng shard)
    private static final String NOTIFICATION_COLLECTION = "notifications";
    private final Firestore firestore;

    public UserDao(Firestore firestore) {
        this.firestore = firestore;
    }

    // Trả về DocumentReference của một user cụ thể
    private DocumentReference getUserDocumentReference(String userId) {
        return firestore.collection(USERS_COLLECTION).document(userId);
    }
    //Thêm Notification cho từng user được thông báo
    public void addNotification(String userId, Notifications notification) {
        DocumentReference userDocumentRef = getUserDocumentReference(userId);
        DocumentReference notificationDocumentRef = userDocumentRef.collection(NOTIFICATION_COLLECTION).document();
        notificationDocumentRef.set(notification);
    }

    // Xoá token FCM khỏi danh sách fcmToken của user
    public void removeFCMToken(String userId, String tokenToRemove) {
        DocumentReference userDocumentRef = getUserDocumentReference(userId);
        userDocumentRef.update("fcmToken", FieldValue.arrayRemove(tokenToRemove));
    }

    // Lấy số lượng shard của danh sách followers của user
    private String getFollowerShardSize(String userId) throws ExecutionException, InterruptedException {
        return getUserDocumentReference(userId)
                .get()
                .get()
                .getString("followerShardSize");
    }

    // Lấy số lượng shard của danh sách following của user
    private String getFollowingShardSize(String userId) throws ExecutionException, InterruptedException {
        return getUserDocumentReference(userId)
                .get()
                .get()
                .getString("followingShardSize");
    }

    // Lấy danh sách các FCM token của user
    private String[] getUserFCMTokens(String userId) throws ExecutionException, InterruptedException {
        return (String[]) getUserDocumentReference(userId)
                .get()
                .get()
                .get("fcmToken");
    }

    // Lấy document reference của shard follower cụ thể
    private DocumentReference getFollowerShardReference(String userId, String shardIndex) {
        return getUserDocumentReference(userId)
                .collection(FOLLOWERS_COLLECTION)
                .document(shardIndex);
    }

    // Lấy danh sách token FCM của các follower có bật thông báo
    public Map<String, String[]> getFCMTokensForNotifiableFollowers(String userId)
            throws ExecutionException, InterruptedException {
        Map<String, String[]> fcmTokens = new HashMap<>();
        List<String> notifiableFollowers = getNotifiableFollowers(userId); // Lấy danh sách follower cần thông báo

        for (String followerId : notifiableFollowers) {
            fcmTokens.put(followerId, getUserFCMTokens(followerId));
        }
        return fcmTokens;
    }

    // Kiểm tra xem follower có cần nhận thông báo từ followee không
    private boolean shouldNotifyFollower(String followerId, String followeeId, String shardSize)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot;
        for (int i = 0; i < Integer.parseInt(shardSize); i++) {
            // Duyệt qua các shard của danh sách following của follower để kiểm tra followee
            snapshot = getUserDocumentReference(followerId)
                    .collection(FOLLOWING_COLLECTION_PREFIX + i)
                    .document(followeeId)
                    .get()
                    .get();
            if (snapshot.exists()) {
                return Boolean.TRUE.equals(snapshot.getBoolean("notifyEnable")); // Nếu có và bật notify thì trả về true
            }
        }
        return false; // Nếu không tìm thấy followee hoặc không bật notify
    }

    // Lấy danh sách followerId từ 1 shard follower cụ thể
    private String[] getFollowersFromShard(String userId, int shardIndex) throws ExecutionException, InterruptedException {
        DocumentReference followerShardRef = getFollowerShardReference(userId, String.valueOf(shardIndex));
        DocumentSnapshot snapshot = followerShardRef.get().get();
        Follower follower = snapshot.toObject(Follower.class);
        return follower != null ? follower.getFollowerId() : new String[0]; // Nếu shard rỗng thì trả về mảng rỗng
    }

    // Lấy danh sách tất cả follower có bật thông báo cho user
    public List<String> getNotifiableFollowers(String userId) throws ExecutionException, InterruptedException {
        String followerShardSize = getFollowerShardSize(userId); // Lấy số shard follower
        List<String> notifiableFollowers = new LinkedList<>();

        for (int i = 0; i < Integer.parseInt(followerShardSize); i++) {
            String[] followerIds = getFollowersFromShard(userId, i + 1); // shard bắt đầu từ 1
            for (String followerId : followerIds) {
                // Nếu follower có bật notify thì thêm vào danh sách
                if (shouldNotifyFollower(followerId, userId, getFollowingShardSize(followerId))) {
                    notifiableFollowers.add(followerId);
                }
            }
        }
        return notifiableFollowers;
    }
}
