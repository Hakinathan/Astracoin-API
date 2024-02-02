package be.helmo.astracoinapi.service;

import be.helmo.astracoinapi.model.entity.Currency;
import be.helmo.astracoinapi.model.entity.Folder;
import be.helmo.astracoinapi.model.entity.MarketData;
import be.helmo.astracoinapi.model.notification.NotificationSenderCloudMessaging;
import be.helmo.astracoinapi.repository.CurrencyRepository;
import be.helmo.astracoinapi.repository.FolderRepository;
import be.helmo.astracoinapi.repository.MarketDataRepository;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationVariationCron {
    Logger logger = LoggerFactory.getLogger(NotificationVariationCron.class);
    @Autowired
    private final CurrencyRepository currencyRepository;
    @Autowired
    private final MarketDataRepository marketDataRepository;
    @Autowired
    private final FolderRepository folderRepository;
    @Autowired
    private final NotificationSenderCloudMessaging notification;

    public NotificationVariationCron(final CurrencyRepository currencyRepository, final MarketDataRepository marketDataRepository, FolderRepository folderRepository, NotificationSenderCloudMessaging notification) {
        this.currencyRepository = currencyRepository;
        this.marketDataRepository = marketDataRepository;
        this.folderRepository = folderRepository;
        this.notification = notification;
    }
    //@Scheduled(cron = "0 0/1 * * * *")
    @Scheduled(cron = "0 0 */5 ? * *")
    public void analyzerVariation(){
        List<Currency> currencies = currencyRepository.findAll();
        for(Currency currency : currencies){
            makeNotification(currency);
        }
    }

    private void makeNotification(Currency currency){
        Double averageValue = marketDataRepository.findAverageValue(currency.getId(), 7);
        MarketData marketData = marketDataRepository.findFirstByCurrency_IdIsOrderByIssuedDesc(currency.getId());
        if(isOnVariation(averageValue,marketData.getEURValue(), 1, -1)){
            logger.info("VARIATION DETECTED " + averageValue + " from " + currency.getName());
            List<Folder> folders = folderRepository.findByCurrency_IdEqualsAndUser_NotifiedIsTrue(currency.getId());
            for(Folder folder : folders){
                try {
                    logger.info("Notification send to " + folder.getUser().getUsername() + "\n Currency : " + currency.getName());
                    notification.sendNotification(folder.getUser(), currency);
                } catch (FirebaseMessagingException e) {
                    logger.warn(e.getMessage());
                }
            }
        }
    }

    public static boolean isOnVariation(double vd, double va, double maxVariation, double minVariation){
        double variation = variation(vd, va);
        return variation >= maxVariation || variation <= minVariation;
    }

    public static double variation(double vd, double va){
        if (vd == va) return 0.;
        return ((va - vd) / vd) * 100;
    }

}
