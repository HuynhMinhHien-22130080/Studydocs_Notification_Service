package com.studydocs.notification.notification_service.event;

public interface EventConsumer<T> {
    void consume(T event);
}
