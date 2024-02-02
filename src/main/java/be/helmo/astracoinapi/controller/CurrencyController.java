package be.helmo.astracoinapi.controller;

import be.helmo.astracoinapi.exception.CurrencyNotFound;
import be.helmo.astracoinapi.exception.UserNotFound;
import be.helmo.astracoinapi.model.entity.Currency;
import be.helmo.astracoinapi.model.entity.MarketData;
import be.helmo.astracoinapi.model.entity.User;
import be.helmo.astracoinapi.repository.CurrencyRepository;
import be.helmo.astracoinapi.repository.MarketDataRepository;
import io.swagger.v3.oas.annotations.Operation;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/currency")
@Tag(name="currency",description = "Currency API")
public class CurrencyController {

    @Autowired
    CurrencyRepository currencyRepository;

    @Operation(summary = "Récupére toutes les cryptomonnaies",description = "Récupére toutes les cryptomonnaies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie",content = @Content(array = @ArraySchema(schema = @Schema(implementation = Currency.class)))),
    } )
    @GetMapping("/list")
    List<Currency> getCurrencies() {
        return currencyRepository.findAll();
    }

    @Operation(summary = "Récupére les données d'une cryptomonnaie",description = "Récupére les données d'une cryptomonnaie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie",content = @Content(schema =  @Schema(implementation = Currency.class))),
    } )
    @GetMapping("/{id}")
    Currency getCurrency(@PathVariable String id) throws CurrencyNotFound {
        Currency currency = currencyRepository.findByIdEquals(id);
        if(currency == null) throw new CurrencyNotFound();
        return currency;
    }

}
