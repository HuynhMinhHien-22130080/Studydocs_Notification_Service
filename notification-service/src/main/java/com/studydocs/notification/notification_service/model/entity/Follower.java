package com.studydocs.notification.notification_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Follower {
    private String shardIndex;
    private String[] followerId;

}
