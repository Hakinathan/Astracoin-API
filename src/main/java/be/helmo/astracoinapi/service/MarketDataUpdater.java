package be.helmo.astracoinapi.service;

import be.helmo.astracoinapi.model.entity.Currency;
import be.helmo.astracoinapi.model.entity.Folder;
import be.helmo.astracoinapi.model.entity.MarketData;
import be.helmo.astracoinapi.repository.CurrencyRepository;
import be.helmo.astracoinapi.repository.FolderRepository;
import be.helmo.astracoinapi.repository.MarketDataRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class MarketDataUpdater {
    Logger logger = LoggerFactory.getLogger(MarketDataUpdater.class);
    @Autowired
    private final CurrencyRepository currencyRepository;
    @Autowired
    private final MarketDataRepository marketDataRepository;

    public MarketDataUpdater(final CurrencyRepository currencyRepository, final MarketDataRepository marketDataRepository) {
        this.currencyRepository = currencyRepository;
        this.marketDataRepository = marketDataRepository;
    }


    @Scheduled(cron = "0 0/15 * * * *")
    public void updateMarketData() {
        List<String> idList = new ArrayList<>();
        for (Currency c : currencyRepository.findAll()) {
            idList.add(c.getId());
        }
        String uri_coingecko = "https://api.coingecko.com/api/v3/simple/price/?ids=" + String.join(",", idList) + "&vs_currencies=eur&eur_24h_change=true";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri_coingecko, String.class);
        logger.info("CRON MARKETUPDATER : " + result);
        JSONObject jsonObject = new JSONObject(result);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        for (Currency c : currencyRepository.findAll()) {
            MarketData marketData = new MarketData();
            marketData.setCurrency(c);
            marketData.setEURValue(Double.parseDouble(jsonObject.getJSONObject(c.getId()).get("eur").toString()));
            marketData.setIssued(timestamp);

            c.setEURValue(Double.parseDouble(jsonObject.getJSONObject(c.getId()).get("eur").toString()));
            c.setIssued(timestamp);
            currencyRepository.save(c);
            marketDataRepository.save(marketData);
        }
    }
}
