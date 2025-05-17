package com.studydocs.notification.notification_service.model.entity;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notifications {
    private String senderId;
    private String documentId;
    private String type;
    private String title;
    private String message;
    @Builder.Default
    private Timestamp createAt = new Timestamp(System.currentTimeMillis());
    @Builder.Default
    private boolean isRead = false;


}
