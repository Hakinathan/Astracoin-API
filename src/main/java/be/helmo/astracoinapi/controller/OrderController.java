package be.helmo.astracoinapi.controller;

import be.helmo.astracoinapi.exception.CurrencyNotFound;
import be.helmo.astracoinapi.exception.ForbiddenException;
import be.helmo.astracoinapi.exception.InsufficientFunds;
import be.helmo.astracoinapi.exception.UserNotFound;
import be.helmo.astracoinapi.model.ResponseJson;
import be.helmo.astracoinapi.model.entity.*;
import be.helmo.astracoinapi.repository.CurrencyRepository;
import be.helmo.astracoinapi.repository.FolderRepository;
import be.helmo.astracoinapi.repository.OrderRepository;
import be.helmo.astracoinapi.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("/order")
@Tag(name="order",description = "Order API")
public class OrderController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Permet de vendre une crypto qui sera automatiquement transférer vers le compte fiat (euro)
     * @param value La valeur source (param coin) à convertir
     * @param coin L'ID de la crypto
     * @return
     * @throws UserNotFound
     * @throws CurrencyNotFound
     * @throws InsufficientFunds
     */
    @Operation(summary = "Permet de vendre une cryptomonnaie",description = "Vend une cryptomonnaie vers une monnaie Fiat (Euro)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(schema = @Schema(implementation = ResponseJson.class))),
            @ApiResponse(responseCode = "404", description = "Cryptmonnaie inconnue ou Utilisateur inconnue ou fond insuffisant"),
            @ApiResponse(responseCode = "403", description = "Compte bloqué")
    } )
    @PostMapping("/sell/crypto")
    ResponseJson sellCrypto(@Parameter(description = "Valeur source") @PathParam("value") float value,@Parameter(description = "ID de la cryptomonnaie source") @PathParam("coin") String coin) throws UserNotFound, CurrencyNotFound, InsufficientFunds, ForbiddenException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByMailEqualsIgnoreCase(authentication.getName());
        if(user == null) throw new UserNotFound();
        if(user.isBlocked()) throw new ForbiddenException();

        Currency currency = currencyRepository.findByIdEquals(coin);
        if(currency == null) throw new CurrencyNotFound();

        Folder folder = folderRepository.findByIdAndCoin(coin, user.getId());

        if(folder == null) throw new InsufficientFunds();
        if(folder.getBalance() < value) throw new InsufficientFunds();
        folder.setBalance(folder.getBalance() - value);
        float euro = (float) (currency.getEURValue() * value);

        user.setWalletEuro(user.getWalletEuro() + euro);

        Order orderSell = new Order();
        orderSell.setFolder(folder);
        orderSell.setTypeOrder(TypeOrder.NOW);
        orderSell.setIssuedAt(new Date(Calendar.getInstance().getTime().getTime()));
        orderSell.setExecutedAt(new Date(Calendar.getInstance().getTime().getTime()));
        orderSell.setDirection(DirectionOrder.SELL);
        orderSell.setValue(value);


        userRepository.save(user);
        folderRepository.save(folder);
        orderRepository.save(orderSell);
        return new ResponseJson("Cryptocurrency sold");
    }

    /**
     * Convertit la crypto vers une autre
     * @param value Valeur de la craypto source à convertir
     * @param fromCoin Crypto source
     * @param toCoin Crypto destination
     * @return
     * @throws UserNotFound
     * @throws InsufficientFunds
     * @throws CurrencyNotFound
     */
    @Operation(summary = "Converti une cryptomonnaie vers une autre cryptomonnaie",description = "Converti une cryptomonnaie vers une autre cryptomonnaie en passant par une monnaie Fiat (Euro)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(schema = @Schema(implementation = ResponseJson.class))),
            @ApiResponse(responseCode = "404", description = "Cryptmonnaie inconnue ou Utilisateur inconnue ou fond insuffisant"),
            @ApiResponse(responseCode = "403", description = "Compte bloqué")
    } )
    @PostMapping("/buy/crypto")
    ResponseJson buyCrypto(@Parameter(description = "Valeur source") @PathParam("value") float value,@Parameter(description = "ID de la Cryptomonnaie source") @PathParam("fromCoin") String fromCoin,@Parameter(description = "Cryptomonnaie cible") @PathParam("toCoin") String toCoin) throws UserNotFound, InsufficientFunds, CurrencyNotFound, ForbiddenException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByMailEqualsIgnoreCase(authentication.getName());
        if(user == null) throw new UserNotFound();
        if(user.isBlocked()) throw new ForbiddenException();

        Folder folderFrom = folderRepository.findByIdAndCoin(fromCoin, user.getId());
        if(folderFrom == null) throw new InsufficientFunds();
        if(folderFrom.getBalance() < value) throw new InsufficientFunds();
        Currency from = currencyRepository.findByIdEquals(fromCoin);
        Currency to = currencyRepository.findByIdEquals(toCoin);
        if(from == null || to == null) throw new CurrencyNotFound();

        Folder folderTo = folderRepository.findByIdAndCoin(toCoin, user.getId());

        folderFrom.setBalance(folderFrom.getBalance() - value);

        float euro = (float) (from.getEURValue() * value);
        float toAdd = (float) ((1 / to.getEURValue()) * euro);
        if(folderTo == null){
            folderTo = new Folder();
            folderTo.setUser(user);
            folderTo.setCurrency(to);
            folderTo.setCreatedAt(new Date(Calendar.getInstance().getTime().getTime()));
            folderTo.setBalance(toAdd);
        }
        else{
            folderTo.setBalance(folderTo.getBalance() + toAdd);
        }

        folderRepository.save(folderFrom);
        folderRepository.save(folderTo);

        // ORDRE DE VENTE

        Order orderSell = new Order();
        orderSell.setFolder(folderFrom);
        orderSell.setTypeOrder(TypeOrder.NOW);
        orderSell.setIssuedAt(new Date(Calendar.getInstance().getTime().getTime()));
        orderSell.setExecutedAt(new Date(Calendar.getInstance().getTime().getTime()));
        orderSell.setDirection(DirectionOrder.SELL);
        orderSell.setValue(value);

        // ORDRE D'ACHAT

        Order orderBuy = new Order();
        orderBuy.setFolder(folderTo);
        orderBuy.setTypeOrder(TypeOrder.NOW);
        orderBuy.setIssuedAt(new Date(Calendar.getInstance().getTime().getTime()));
        orderBuy.setExecutedAt(new Date(Calendar.getInstance().getTime().getTime()));
        orderBuy.setDirection(DirectionOrder.BUY);
        orderBuy.setValue(toAdd);

        orderRepository.save(orderSell);
        orderRepository.save(orderBuy);
        return new ResponseJson("Converted cryptocurrency");
    }

    /**
     * Convert a fiat currency (EUR) to CryptoCurrency
     * @param value Fiat Value (EUR)
     * @param toCoin CryptoCurrency destination
     * @return
     * @throws UserNotFound
     * @throws CurrencyNotFound
     */
    @Operation(summary = "Permet d'acheter des cryptomonnaies",description = "Permet d'acheter des cryptomonnaies en convertissant le compte Euro (Fiat)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(schema = @Schema(implementation = ResponseJson.class))),
            @ApiResponse(responseCode = "404", description = "Cryptmonnaie inconnue ou Utilisateur inconnue ou fond insuffisant"),
            @ApiResponse(responseCode = "403", description = "Compte bloqué")
    } )
    @PostMapping("buy/fiat")
    ResponseJson buyFiat(@Parameter(description = "Valeur en Euro") @PathParam("value") float value, @PathParam("toCoin") String toCoin) throws UserNotFound, CurrencyNotFound, InsufficientFunds, ForbiddenException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByMailEqualsIgnoreCase(authentication.getName());
        if(user == null) throw new UserNotFound(); // Vérification si l'utilisateur existe quand l'action est demandée
        if(user.isBlocked()) throw new ForbiddenException();
        if(user.getWalletEuro() < value) throw new InsufficientFunds(); // Vérification l'achat de crypto ne dépasse pas les fonds
        Currency currency = currencyRepository.findByIdEquals(toCoin);
        if(currency == null) throw new CurrencyNotFound(); // Vérification si l'ID de la crypto existe
        Folder folder = folderRepository.findByIdAndCoin(toCoin, user.getId());
        float toAdd = (float) ((1 / currency.getEURValue()) * value);
        if(folder == null){ // Il n'existe aucun portefeuille de la crypto en question
            folder = new Folder();
            folder.setUser(user);
            folder.setCurrency(currency);
            folder.setCreatedAt(new Date(Calendar.getInstance().getTime().getTime()));
            folder.setBalance(toAdd);
        }
        else{
            folder.setBalance(folder.getBalance() + toAdd);
        }
        user.setWalletEuro(user.getWalletEuro() - value);
        Order order = new Order();
        order.setFolder(folder);
        order.setTypeOrder(TypeOrder.NOW);
        order.setIssuedAt(new Date(Calendar.getInstance().getTime().getTime()));
        order.setExecutedAt(new Date(Calendar.getInstance().getTime().getTime()));
        order.setDirection(DirectionOrder.BUY);
        order.setValue(value);
        folderRepository.save(folder);
        userRepository.save(user);
        orderRepository.save(order);
        return new ResponseJson("Cryptocurrency purchased");
    }

    @Operation(summary = "Récupérer la liste de tous les ordres déposés",description = "Affiche la liste de tous les ordres déposés trié par la date d'exécution", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie",content = @Content(array = @ArraySchema(schema = @Schema(implementation = Order.class)))),
            @ApiResponse(responseCode = "404", description = "Utilisateur inconnue"),
    } )
    @GetMapping("/list")
    List<Order> getOrder() throws UserNotFound {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByMailEqualsIgnoreCase(authentication.getName());
        if(user == null) throw new UserNotFound(); // Vérification si l'utilisateur existe quand l'action est demandée

        return orderRepository.findByFolder_User_Id(user.getId());
    }


}
