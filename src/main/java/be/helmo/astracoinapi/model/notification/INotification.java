package be.helmo.astracoinapi.model.notification;

import be.helmo.astracoinapi.model.entity.Currency;
import be.helmo.astracoinapi.model.entity.User;
import com.google.firebase.messaging.FirebaseMessagingException;

public interface INotification {
    void sendNotification(User user, Currency currency) throws FirebaseMessagingException;
    void sendNotification(User user, Currency currency, NotificationType type) throws FirebaseMessagingException;
    void sendNotification(User user, Currency currency, NotificationType type, String customMessage) throws FirebaseMessagingException;

}
