package com.studydocs.notification.notification_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Documents {
    private String documentId;
    private String userName;
    private String title;
    private Timestamp createAt;
    public String getDescription() {
        return "Tài liệu có mã số: " + documentId + " với tiêu đề \"" + title + "\" được tạo bởi " + userName + " vào lúc " + createAt.toString() + ".";
    }
}
