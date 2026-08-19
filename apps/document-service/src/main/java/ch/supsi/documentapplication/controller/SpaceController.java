package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO;
import ch.supsi.documentapplication.controller.DTO.request.UpdateSpaceRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.*;
import ch.supsi.documentapplication.model.Space;
import ch.supsi.documentapplication.service.ISpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/spaces")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Spaces", description = "Endpoints for handling spaces (fsDocument aggregate)")
public class SpaceController {

    private final ISpaceService spaceService;

    @Autowired
    public SpaceController(ISpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates a space", description = "Creates a space with the specified name and description.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "204", description = "Space created"),
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<@NotNull CreateSpaceResponseDTO> createSpace(@Valid @Parameter(description = "The name and description of the space to be created", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) @RequestBody CreateSpaceRequestDTO createSpaceRequestDTO,
                                                                       @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        var space = new Space(username, createSpaceRequestDTO.getIcon(), createSpaceRequestDTO.getName(), createSpaceRequestDTO.getDescription());
        Long id = spaceService.create(space);
        return ResponseEntity.ok(new CreateSpaceResponseDTO(id));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieves space information", description = "Retrieves a space id, name and description.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Space retrieved"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<@NotNull List<@Valid ReadSpaceResponseDTO>> readSpaces(@Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var spaces = spaceService.getSpaces(username);
        var spaceDTOs = spaces.stream().map(space -> new ReadSpaceResponseDTO(space.getId(), space.getIcon(), space.getName(), space.getDescription())).toList();

        return ResponseEntity.ok(spaceDTOs);
    }

    @DeleteMapping(value = "/{spaceId}")
    @Operation(summary = "Deletes a space", description = "Deletes a space by id.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "204", description = "Space deleted"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<Void> deleteSpace(@Parameter(hidden = true) Authentication authentication,
                                            @PathVariable Long spaceId) {
        String username = authentication.getName();
        spaceService.delete(username, spaceId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{spaceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates a space", description = "Updates a space name and description by id.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "204", description = "Space updated"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<Void> updateSpace(@Parameter(hidden = true) Authentication authentication,
                                            @PathVariable Long spaceId,
                                            @Valid @RequestBody @NotNull UpdateSpaceRequestDTO request) {
        String username = authentication.getName();
        var space = new Space(spaceId, username, request.icon(), request.name(), request.description());
        spaceService.update(space);
        return ResponseEntity.noContent().build();
    }
}
