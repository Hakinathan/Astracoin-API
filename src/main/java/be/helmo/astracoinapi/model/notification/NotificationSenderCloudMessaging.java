package be.helmo.astracoinapi.model.notification;

import be.helmo.astracoinapi.model.entity.Currency;
import be.helmo.astracoinapi.model.entity.User;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationSenderCloudMessaging implements INotification{

    private final FirebaseMessaging firebaseMessaging;

    public NotificationSenderCloudMessaging(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public String sendMessage(String fcm, String currency, String messageText) throws FirebaseMessagingException {

        Notification builder = Notification
                .builder()
                .setTitle("Mouvement sur la crypto " + currency)
                .setBody(messageText)
                .build();

        Message message = Message
                .builder()
                .setToken(fcm)
                .setNotification(builder)
                .putData("title", "Mouvement sur la crypto " + currency)
                .putData("body", messageText)
                .build();

        return firebaseMessaging.send(message);

    }

    @Override
    public void sendNotification(User user, Currency currency) throws FirebaseMessagingException {
        sendMessage(user.getFcm(), currency.getName(), "Mouvement de +/- 1%");
    }

    @Override
    public void sendNotification(User user, Currency currency, NotificationType type) throws FirebaseMessagingException {
       sendMessage(user.getFcm(), currency.getName(), type.getMsg());
    }

    @Override
    public void sendNotification(User user, Currency currency, NotificationType type, String customMessage) throws FirebaseMessagingException {

    }
}
