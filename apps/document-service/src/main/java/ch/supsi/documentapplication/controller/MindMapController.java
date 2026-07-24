package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.response.ErrorResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.MindMapResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.MindMapSummaryDTO;
import ch.supsi.documentapplication.controller.DTO.response.MindMapsResponseDTO;
import ch.supsi.documentapplication.service.ISpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/spaces/{spaceId}/mind-maps")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Mind-Maps", description = "Endpoints for handling mind-maps within a space")
public class MindMapController {

    private final ISpaceService spaceService;

    @Autowired
    public MindMapController(ISpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a mind-map for the specified space", description = """
            Starts asynchronous mind-map generation for the specified space.
            The mind-map row is created synchronously and the response includes its id and GENERATING status.
            Node generation continues in the background.
            """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "202", description = "Mind-map generation accepted"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or the space cannot be processed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "409", description = "A mind-map generation is already in progress for this space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<MindMapSummaryDTO> extractMindMap(
            @Parameter(description = "The ID of the space to create the mind map for", required = true)
                    @NotNull @PathVariable("spaceId") Long spaceId,
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var summary = spaceService.createMindMap(username, spaceId);
        return ResponseEntity.accepted().body(summary);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List mind-maps for the specified space", description = """
            Returns the mind maps associated with the specified space, including generation status.
            The list is empty when no mind map has been created yet.
            """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Mind maps retrieved successfully"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<MindMapsResponseDTO> listMindMaps(
            @Parameter(description = "The ID of the space to list mind maps for", required = true)
                    @NotNull @PathVariable("spaceId") Long spaceId,
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var mindMaps = spaceService.listMindMaps(username, spaceId);
        return ResponseEntity.ok(new MindMapsResponseDTO(mindMaps));
    }

    @GetMapping(value = "/{mindMapId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a mind-map by id", description = """
            Returns the mind map for the specified space and id, including generation status and nodes.
            Returns 404 when the mind map does not exist or does not belong to the space.
            """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Mind-map retrieved successfully"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space or mind-map not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<MindMapResponseDTO> getMindMap(
            @Parameter(description = "The ID of the space", required = true) @NotNull @PathVariable("spaceId")
                    Long spaceId,
            @Parameter(description = "The ID of the mind map", required = true) @NotNull @PathVariable("mindMapId")
                    Long mindMapId,
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var mindMap = spaceService.getMindMap(username, spaceId, mindMapId);
        var status = spaceService.getMindMapStatus(username, spaceId);
        return ResponseEntity.ok(MindMapResponseDTO.from(mindMap, status));
    }

    @DeleteMapping("/{mindMapId}")
    @Operation(summary = "Delete the mind-map for the specified space", description = """
            Deletes the mind-map associated with the specified space and id.
            Returns 404 when the mind map does not exist or does not belong to the space.
            If a generation job is currently running for this mind-map, it will be stopped first.
            """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "204", description = "Mind-map deleted"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space or mind-map not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<Void> deleteMindMap(
            @Parameter(description = "The ID of the space", required = true) @NotNull @PathVariable("spaceId")
                    Long spaceId,
            @Parameter(description = "The ID of the mind map", required = true) @NotNull @PathVariable("mindMapId")
                    Long mindMapId,
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        spaceService.deleteMindMap(username, spaceId, mindMapId);
        return ResponseEntity.noContent().build();
    }
}
