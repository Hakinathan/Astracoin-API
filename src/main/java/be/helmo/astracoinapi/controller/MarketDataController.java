package be.helmo.astracoinapi.controller;

import be.helmo.astracoinapi.exception.CurrencyNotFound;
import be.helmo.astracoinapi.model.entity.Currency;
import be.helmo.astracoinapi.model.entity.MarketData;
import be.helmo.astracoinapi.repository.CurrencyRepository;
import be.helmo.astracoinapi.repository.MarketDataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/marketdata")
@Tag(name="market data",description = "Market Data API")
public class MarketDataController {
    @Autowired
    MarketDataRepository marketDataRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    /**
     * Return last 8 hours data market from specific crypto
     * @param id ID Crypto
     * @return
     */
    @Operation(summary = "Retourne les données de marché",description = "Retourner les données de marché d'une cryptomonnaie sur les 8 dernières heures")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MarketData.class)))),
            @ApiResponse(responseCode = "404", description = "Cryptmonnaie inconnue")
    } )
    @GetMapping("/{id}")
    List<MarketData> getMarketData(@Parameter(description = "ID de la cryptomonnaie") @PathVariable String id) throws CurrencyNotFound {
        Currency currency = currencyRepository.findByIdEquals(id);
        if(currency == null) throw new CurrencyNotFound();
        return marketDataRepository.findMarketDataByCurrency(id, new Date(System.currentTimeMillis() - ((3600 * 1000) * 8)));
    }
}
