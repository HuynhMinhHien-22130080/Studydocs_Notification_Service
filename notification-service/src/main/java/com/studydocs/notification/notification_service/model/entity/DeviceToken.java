package com.studydocs.notification.notification_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
public class DeviceToken {
    private String token;
    private Timestamp lastLogin;

    /**
     * Kiểm tra thiết bị có hoạt động trong vòng số tháng gần nhất hay không.
     *
     * @param months Số tháng cần kiểm tra.
     * @return true nếu thiết bị hoạt động trong vòng <code>months</code> tháng gần nhất, ngược lại false.
     */
    public boolean isActiveWithinMonths(int months) {
        if (lastLogin == null) return false;

        LocalDateTime lastLoginTime = lastLogin.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return ChronoUnit.MONTHS.between(lastLoginTime, LocalDateTime.now()) < months;
    }


}
