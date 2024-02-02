package be.helmo.astracoinapi.controller;

import be.helmo.astracoinapi.exception.*;
import be.helmo.astracoinapi.model.ResponseJson;
import be.helmo.astracoinapi.model.entity.*;
import be.helmo.astracoinapi.repository.CurrencyRepository;
import be.helmo.astracoinapi.repository.FolderRepository;
import be.helmo.astracoinapi.repository.UserRepository;
import be.helmo.astracoinapi.utils.PasswordHash;
import com.google.gson.Gson;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Tag(name="user",description = "User API")
public class UserController {
    Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private JavaMailSender emailSender;


    @Value("${jwt.secretkey}")
    private String secretKey;

    @Value("${oauth.discord.clientid}")
    private String discordClientId;
    @Value("${oauth.discord.clientsecret}")
    private String discordClientSecret;
    @Value("${oauth.discord.grandtype}")
    private String grandType;
    @Value("${oauth.discord.redirecturi}")
    private String redirectUri;


    @Operation(summary = "Permet de se connecter via une authentification basique",description = "Permet de se connecter et récupérer le JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(schema = @Schema(implementation = ResponseJson.class))),
            @ApiResponse(responseCode = "403", description = "Email et Mot de passe associé à aucun compte ou compte bloqué")
    } )
    @PostMapping("/login")
    public User login(@RequestParam("email") String email, @RequestParam("password") String password) throws EmailOrPasswordIsNotMatching, ForbiddenException {
        User user = userRepository.findByMailEqualsIgnoreCase(email);
        if (user == null){
            throw new EmailOrPasswordIsNotMatching();
        }
        if(!user.getPassword().equals(new PasswordHash().hash(password))){
            throw new EmailOrPasswordIsNotMatching();
        }
        if(user.isBlocked()) {
            throw new ForbiddenException();
        }
        String token = getJWTToken(email, user.getRole().getName());
        logger.debug("JWT TOKEN LOGGIN : "+ token );
        User logging = new User();
        logging.setMail(email);
        logging.setToken(token);
        logging.setUsername(user.getUsername());
        logging.setRole(user.getRole());
        logging.setNotified(user.isNotified());
        return logging;
    }

    @Operation(summary = "Permet d'enregistrer un utilisateur",description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(schema = @Schema(implementation = ResponseJson.class))),
            @ApiResponse(responseCode = "404", description = "Email déjà utilisé"),
    } )
    @PostMapping("/register")
    public ResponseJson register(@RequestParam("username") String username, @RequestParam("email") String email, @RequestParam("password") String password) throws EmailAlreadyUsed {
        User user = userRepository.findByMailEqualsIgnoreCase(email);
        if(user != null){
            throw new EmailAlreadyUsed();
        }

        User newUser = createNewUser(email, username, password);
        userRepository.save(newUser);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("astracoin@drender.be");
        message.setTo(email);
        message.setSubject("Confirmation d'inscription sur Astracoin");
        message.setText("Bonjour " + username + ",\n\n Merci de vous êtes inscrit sur Astracoin!");
        emailSender.send(message);
        return new ResponseJson("Register successful");
    }

    private String getJWTToken(String username, String role) {
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList(role.toUpperCase(Locale.ROOT));

        String token = Jwts
                .builder()
                .setId("astracoinJWT")
                .setSubject(username)
                .claim("authorities",
                        grantedAuthorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1200000000))
                .signWith(SignatureAlgorithm.HS512,
                        secretKey.getBytes()).compact();

        return token;
    }

    @Operation(summary = "Permet de changer le status de notification",description = "Permet à un utilisateur d'être notifié ou pas", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(schema = @Schema(implementation = ResponseJson.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur inconnue"),
    } )
    @PostMapping("user/notification")
    ResponseJson setNotification(@Parameter(description = "1 = Notifié, 2 = Ne pas être notifié") @RequestParam("state") boolean state) throws UserNotFound {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByMailEqualsIgnoreCase(authentication.getName());
        if(user == null) throw new UserNotFound(); // Vérification si l'utilisateur existe quand l'action est demandée

        user.setNotified(state);

        userRepository.save(user);

        return new ResponseJson("Notification updated");
    }

    @Operation(summary = "Récupére le classement des utilisateurs",description = "Récupére le classement des utilisateurs par ordre de valeur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie",content = @Content(array = @ArraySchema(schema = @Schema(implementation = Ranking.class)))),
    } )
    @GetMapping("user/ranking")
    List<Ranking> getRanking() {
        List<Ranking> result = new ArrayList<>();
        List<User> userList = this.userRepository.findAll();
        for (User user: userList) {
            float total = 0;
            for (Folder folder : this.folderRepository.findById(user.getId())) {
                total += folder.getCurrency().getEURValue()*folder.getBalance();
            }

            if(result.size() <= 100)
                result.add(new Ranking(total, user));
        }

        Collections.sort(result);
        return result;
    }

    @Operation(summary = "Récupére la liste des portefeuilles de l'utilisateur courant", description = "", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie",content = @Content(array = @ArraySchema(schema = @Schema(implementation = Folder.class)))),
    } )
    @GetMapping("user/folders")
    List<Folder> getFolders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return this.folderRepository.findByEmail(authentication.getName());
    }

    @Operation(summary = "Retourne la valeur du portefeuille Euro de l'utilisateur courant",description = "", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie",content = @Content(schema = @Schema(implementation = ResponseJson.class))),
    } )
    @GetMapping("user/wallet")
    ResponseJson getBalance() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new ResponseJson(String.valueOf(this.userRepository.findByMailEqualsIgnoreCase(authentication.getName()).getWalletEuro()));
    }


    @Operation(summary = "Permet de se connecter via Discord OAuth",description = "Permet de se connecter par Discord OAuth et récupérer le JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "403", description = "Email et Mot de passe associé à aucun compte ou compte bloqué")
    } )
    @PostMapping("callback/discord")
    User callbackDiscord(@RequestParam("code") String code) throws ForbiddenException, InternalErrorException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "code=" + code + "&client_id=" + discordClientId +"&client_secret=" + discordClientSecret + "&grant_type=" + grandType + "&redirect_uri=" + redirectUri);
        Request request = new Request.Builder()
                .url("https://discord.com/api/v8/oauth2/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try {
            Response response = client.newCall(request).execute();

            Gson gson = new Gson();
            ResponseOAuth entity = gson.fromJson(response.body().string(), ResponseOAuth.class);

            OkHttpClient data = new OkHttpClient().newBuilder()
                    .build();
            Request requestData = new Request.Builder()
                    .url("https://discord.com/api/v8/users/@me")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + entity.getAccess_token())
                    .build();
            Response responseData = client.newCall(requestData).execute();

            ResponseToken responseToken = gson.fromJson(responseData.body().string(), ResponseToken.class);

            logger.debug("Email : " + responseToken.getEmail());
            User user = userRepository.findByMailEqualsIgnoreCase(responseToken.getEmail());
            logger.debug("User : " + user );
            if (user == null){
                logger.debug("User created");
                user = createNewUser(responseToken.getEmail(), responseToken.getUsername(), alphaNumericString(10));
                userRepository.save(user);
            }
            if(user.isBlocked()) {
                throw new ForbiddenException();
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("astracoin@drender.be");
            message.setTo(user.getMail());
            message.setSubject("Confirmation d'inscription sur Astracoin");
            message.setText("Bonjour " + user.getUsername() + ",\n\n Merci de vous êtes inscrit sur Astracoin!");
            emailSender.send(message);

            String token = getJWTToken(user.getMail(), user.getRole().getName());
            User logging = new User();
            logging.setMail(user.getMail());
            logging.setToken(token);
            logging.setUsername(user.getUsername());
            logging.setRole(user.getRole());
            return logging;
        } catch (IOException e) {
            throw new InternalErrorException("Error with Discord");
        }
    }

    private User createNewUser(String email, String username, String password){
        User newUser = new User();
        newUser.setMail(email);
        newUser.setPassword(new PasswordHash().hash(password));
        newUser.setUsername(username);
        newUser.setNotified(true);
        newUser.setCreateAt(new Date(Calendar.getInstance().getTime().getTime()));
        newUser.setRole(new Role((short) 1,"default"));
        newUser.setWalletEuro(500000L);

        return newUser;
    }

    /**
     * Génere un mot de passe au hasard pour les enregistrements via OAuth
     * @param len
     * @return
     */
    private static String alphaNumericString(int len) {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rnd = new Random();

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    @Operation(summary = "Permet de mettre à jour le toker FCM en base de donnée", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(schema = @Schema(implementation = ResponseEntity.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable ou token invalide")
    } )
    @PostMapping("user/save-token")
    private ResponseEntity<String> saveToken(@RequestParam("token") String token) throws UserNotFound, FCMTokenException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByMailEqualsIgnoreCase(authentication.getName());
        if(user == null) throw new UserNotFound(); // Vérification si l'utilisateur existe quand l'action est demandée
        if(token == null) throw new FCMTokenException();
        if(token.equals("")) throw new FCMTokenException();

        user.setFcm(token);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body("token saved successfully");
    }
    @Operation(summary = "Envoie un email aux admins",description = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie", content = @Content(schema = @Schema(implementation = ResponseJson.class))),
    } )
    @PostMapping("/contact")
    ResponseJson contact(@RequestParam("message") String text, @RequestParam("subject") String subject, @RequestParam("mail") String mail){

        List<User> admins = userRepository.findByRole(new Role((short) 2, "admin"));

        for(User admin : admins){
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("astracoin@drender.be");
            message.setTo(admin.getMail());
            message.setSubject("Formulaire de contact : " + subject);
            message.setText("FORMULAIRE DE CONTACT  : \n" + "De : " + mail + "\nMessage : " + text);
            emailSender.send(message);
        }

        return new ResponseJson("Successfull sended");
    }
}
