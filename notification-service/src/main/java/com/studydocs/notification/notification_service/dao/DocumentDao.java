package com.studydocs.notification.notification_service.dao;

import com.google.cloud.firestore.Firestore;
import com.studydocs.notification.notification_service.model.entity.Documents;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class DocumentDao {
    private final Firestore firestore;
    private static final String DOCUMENT_COLLECTION = "documents";

    public DocumentDao(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Lấy tài liệu theo id
     */
    public Documents getById(String documentId) throws ExecutionException, InterruptedException {
        return firestore.collection(DOCUMENT_COLLECTION).document(documentId).get().get().toObject(Documents.class);
    }
}
