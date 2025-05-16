package com.studydocs.notification.notification_service.dao;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.studydocs.notification.notification_service.model.entity.Follower;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Data Access Object for managing user-related operations in the Firestore.
 * Handles user followers, following relationships, and FCM tokens.
 */
@Component
public class UserDao {
    private static final String USERS_COLLECTION = "users";
    private static final String FOLLOWERS_COLLECTION = "followers";
    private static final String FOLLOWING_COLLECTION_PREFIX = "following_";

    private final Firestore firestore;

    public UserDao(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Gets a reference to a user document in Firestore.
     *
     * @param userId The ID of the user
     * @return DocumentReference to the user document
     */
    private DocumentReference getUserDocumentReference(String userId) {
        return firestore.collection(USERS_COLLECTION).document(userId);
    }

    @CacheEvict(key = "'fcmTokensForFollowersOf:' + #userId", value = "fcmTokens")
    public void removeFCMToken(String userId, String tokenToRemove) {
        DocumentReference userDocumentRef = getUserDocumentReference(userId);
        userDocumentRef.update("fcmToken", FieldValue.arrayRemove(tokenToRemove));
    }


    /**
     * Retrieves the follower shard size for a user.
     *
     * @param userId The ID of the user
     * @return The follower shard size as a string
     */
    private String getFollowerShardSize(String userId) throws ExecutionException, InterruptedException {
        return getUserDocumentReference(userId)
                .get()
                .get()
                .getString("followerShardSize");
    }

    /**
     * Retrieves the following shard size for a user.
     *
     * @param userId The ID of the user
     * @return The following shard size as a string
     */
    private String getFollowingShardSize(String userId) throws ExecutionException, InterruptedException {
        return getUserDocumentReference(userId)
                .get()
                .get()
                .getString("followingShardSize");
    }

    /**
     * Retrieves the FCM tokens for a user.
     *
     * @param userId The ID of the user
     * @return Array of FCM tokens
     */
    private String[] getUserFCMTokens(String userId) throws ExecutionException, InterruptedException {
        return (String[]) getUserDocumentReference(userId)
                .get()
                .get()
                .get("fcmToken");
    }

    /**
     * Gets a reference to a follower shard document.
     *
     * @param userId     The ID of the user
     * @param shardIndex The shard index
     * @return DocumentReference to the follower shard
     */
    private DocumentReference getFollowerShardReference(String userId, String shardIndex) {
        return getUserDocumentReference(userId)
                .collection(FOLLOWERS_COLLECTION)
                .document(shardIndex);
    }

    /**
     * Retrieves FCM tokens for all followers who should be notified.
     *
     * @param userId The ID of the user
     * @return Map of follower IDs to their FCM tokens
     */
    @Cacheable(
            key = "'fcmTokensForFollowersOf:' + #userId",
            value = "fcmTokens",
            unless = "#result.isEmpty()|| #result==null",
            sync = true

    )
    public Map<String, String[]> getFCMTokensForNotifiableFollowers(String userId)
            throws ExecutionException, InterruptedException {
        Map<String, String[]> fcmTokens = new HashMap<>();
        List<String> notifiableFollowers = getNotifiableFollowers(userId);

        for (String followerId : notifiableFollowers) {
            fcmTokens.put(followerId, getUserFCMTokens(followerId));
        }
        return fcmTokens;
    }

    /**
     * Checks if a follower needs to be notified about a user's activities.
     *
     * @param followerId The ID of the follower
     * @param followeeId The ID of the user being followed
     * @param shardSize  The shard size for the following relationships
     * @return true if notifications are needed, false otherwise
     */
    private boolean shouldNotifyFollower(String followerId, String followeeId, String shardSize)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot;
        for (int i = 0; i < Integer.parseInt(shardSize); i++) {
            snapshot = getUserDocumentReference(followerId)
                    .collection(FOLLOWING_COLLECTION_PREFIX + i)
                    .document(followeeId)
                    .get()
                    .get();
            if (snapshot.exists()) {
                return Boolean.TRUE.equals(snapshot.getBoolean("needNotify"));
            }
        }
        return false;
    }

    /**
     * Retrieves followers from a specific shard.
     *
     * @param userId     The ID of the user
     * @param shardIndex The shard index
     * @return Array of follower IDs
     */
    private String[] getFollowersFromShard(String userId, int shardIndex) throws ExecutionException, InterruptedException {
        DocumentReference followerShardRef = getFollowerShardReference(userId, String.valueOf(shardIndex));
        DocumentSnapshot snapshot = followerShardRef.get().get();
        Follower follower = snapshot.toObject(Follower.class);
        return follower != null ? follower.getFollowerId() : new String[0];
    }

    /**
     * Retrieves all followers who should be notified by a user.
     *
     * @param userId The ID of the user
     * @return List of follower IDs who should be notified
     */
    @Cacheable(
            key = "'notifiableFollowers:' + #userId",
            value = "notifiableFollowers",
            unless = "#result.isEmpty()|| #result==null",
            sync = true
    )
    public List<String> getNotifiableFollowers(String userId) throws ExecutionException, InterruptedException {
        String followerShardSize = getFollowerShardSize(userId);
        List<String> notifiableFollowers = new LinkedList<>();

        for (int i = 0; i < Integer.parseInt(followerShardSize); i++) {
            String[] followerIds = getFollowersFromShard(userId, i + 1);
            for (String followerId : followerIds) {
                if (shouldNotifyFollower(followerId, userId, getFollowingShardSize(followerId))) {
                    notifiableFollowers.add(followerId);
                }
            }
        }
        return notifiableFollowers;
    }
}
