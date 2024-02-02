package be.helmo.astracoinapi.controller;

import be.helmo.astracoinapi.exception.ForbiddenException;
import be.helmo.astracoinapi.exception.UserNotFound;
import be.helmo.astracoinapi.model.entity.*;
import be.helmo.astracoinapi.model.notification.NotificationSenderCloudMessaging;
import be.helmo.astracoinapi.repository.FolderRepository;
import be.helmo.astracoinapi.repository.OrderRepository;
import be.helmo.astracoinapi.repository.UserRepository;
import be.helmo.astracoinapi.utils.PasswordHash;
import com.google.firebase.messaging.FirebaseMessagingException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name="admin",description = "Admin API")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private NotificationSenderCloudMessaging notification;

    @Operation(summary = "Permet de changer l'état de blocage d'un utilisateur",description = "Utilisateur en fonction de son id", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/block", produces = { "application/json" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    } )
    public ResponseEntity<String> block(@Parameter(description = "ID de l'utilisateur") @RequestParam("id") long id,@Parameter(description = "True : Bloqué | False : Débloqué") @RequestParam("value") boolean value) throws UserNotFound, ForbiddenException {
        User user = userRepository.findById(id);

        if(user == null) throw new UserNotFound();

        user.setBlocked(value);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body("successful operation");
    }

    @Operation(summary = "Récupére les données d'un utilisateur",description = "Utilisateur en fonction de son id", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    } )
    @GetMapping(value = "/user/{id}", produces = { "application/json" })
    User one(@Parameter(description = "ID de l'utilisateur") @PathVariable Long id) throws UserNotFound {
        User user = userRepository.findById(id);
        if(user == null) throw new UserNotFound();

        return user;
    }
    @Operation(summary = "Supprime un utilisateur",description = "Supprime un utilisateur et toutes les données associées", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    } )
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/user/{id}/delete", method = RequestMethod.POST)
    ResponseEntity<String> deleteUser(@Parameter(description = "ID de l'utilisateur") @PathVariable Long id) throws UserNotFound {
        User user = userRepository.findById(id);

        if(user == null) throw new UserNotFound();


        List<Order> orderList = orderRepository.findByFolder_User_Id(id);
        List<Folder> folderList = folderRepository.findById(id);

        orderRepository.deleteAll(orderList);
        folderRepository.deleteAll(folderList);
        userRepository.delete(user);

        return ResponseEntity.status(HttpStatus.OK).body("successful operation");
    }
    @Operation(summary = "Permet de modifier les données d'un utilisateur",description = "Supprime un utilisateur et toutes les données associées en fonction de l'id", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    } )
    @PostMapping("/user/{id}/edit")
    ResponseEntity<String> editUser(@Parameter(description = "ID de l'utilisateur") @PathVariable long id,@Parameter(description = "Nouveau pseudo") @RequestParam("username") String username,@Parameter(description = "Nouveau mot de passe") @RequestParam("password") String password) throws UserNotFound {
        User user = userRepository.findById(id);

        if(user == null) throw new UserNotFound();

        user.setUsername(username);
        user.setPassword(new PasswordHash().hash(password));
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body("successful operation");
    }
    @Operation(summary = "Récupére tous les utilisateurs",description = "Récupérer tout les utilisateurs", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie",content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
    } )
    @GetMapping("/users")
    List<User> all() {
        return userRepository.findAll();
    }

    /**
     * ENDPOINT FOR TEST ONLY
     * @param id
     * @return
     * @throws UserNotFound
     */
    @Operation(summary = "[TEST ONLY] Permet d'envoyer une notification test",description = "Envoie une notification test à un utilisateur spécifique", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    } )
    @GetMapping("/user/{id}/notify")
    ResponseEntity<String> notify(@Parameter(description = "ID de l'utilisateur")@PathVariable long id) throws UserNotFound, FirebaseMessagingException {
        User user = userRepository.findById(id);

        if(user == null) throw new UserNotFound();
        Currency currency = new Currency();
        currency.setName("HELMOCOIN");
        notification.sendNotification(user, currency);

        return ResponseEntity.status(HttpStatus.OK).body("successful operation");
    }

}

