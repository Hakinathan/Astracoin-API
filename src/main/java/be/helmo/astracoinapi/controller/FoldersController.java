package be.helmo.astracoinapi.controller;

import be.helmo.astracoinapi.exception.UserNotFound;
import be.helmo.astracoinapi.model.entity.Currency;
import be.helmo.astracoinapi.model.entity.Folder;
import be.helmo.astracoinapi.repository.FolderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name="folder",description = "Folder API")
public class FoldersController {
    @Autowired
    private FolderRepository folderRepository;

    @Operation(summary = "Récupére le portefeuille",description = "Récupére le portefeuille d'une cryptomonnaie et d'un utilisateur spécifique", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Opération réussie",content = @Content(schema =  @Schema(implementation = Currency.class))),
            @ApiResponse(responseCode = "404", description = "Aucun Portefeuille associé")
    } )
    @GetMapping("/folder/{id}/{id_c}")
    Folder one(@Parameter(description = "ID de l'utilisateur") @PathVariable Long id,@Parameter(description = "ID de la cryptomonnaie") @PathVariable String id_c) throws UserNotFound {
        Folder folder = this.folderRepository.findByIdAndCoin(id_c,id);
        if(folder == null) throw new UserNotFound();
        return folder;
    }
}
