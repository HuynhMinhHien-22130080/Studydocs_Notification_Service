package com.studydocs.notification.notification_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Followers {
    private String userId;
    private boolean notifyOnNewPost;
}
