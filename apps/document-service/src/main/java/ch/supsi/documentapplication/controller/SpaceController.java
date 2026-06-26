package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO;
import ch.supsi.documentapplication.controller.DTO.request.UpdateSpaceRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.MindMapResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.MindMapStatusDTO;
import ch.supsi.documentapplication.controller.DTO.response.ReadSpaceResponseDTO;
import ch.supsi.documentapplication.model.DocumentStatus;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.properties.AppProperties;
import ch.supsi.documentapplication.service.ISpaceService;
import ch.supsi.documentapplication.service.document.IDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/spaces")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Spaces", description = "Endpoints for handling spaces (document aggregate)")
public class SpaceController {

    private final AppProperties appProperties;
    private final ISpaceService spaceService;
    private final IDocumentService documentService;

    @Autowired
    public SpaceController(AppProperties appProperties, ISpaceService spaceService, IDocumentService documentService) {
        this.appProperties = appProperties;
        this.spaceService = spaceService;
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates a space", description = "Creates a space with the specified name and description.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Space created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<@NotNull CreateSpaceResponseDTO> createSpace(
            @Parameter(
                            description = "The name and description of the space to be created",
                            required = true,
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
                    @RequestBody
                    CreateSpaceRequestDTO createSpaceRequestDTO,
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        Long id =
                spaceService.create(username, createSpaceRequestDTO.getName(), createSpaceRequestDTO.getDescription());
        return ResponseEntity.ok(new CreateSpaceResponseDTO(id));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieves space information", description = "Retrieves a space id, name and description.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Space retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<@NotNull List<ReadSpaceResponseDTO>> readSpaces(
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var spaces = spaceService.getSpaces(username);
        var spaceDTOs = spaces.stream()
                .map(space -> new ReadSpaceResponseDTO(space.getId(), space.getName(), space.getDescription()))
                .toList();

        return ResponseEntity.ok(spaceDTOs);
    }

    @DeleteMapping(value = "/{spaceId}")
    @Operation(summary = "Deletes a space", description = "Deletes a space by id.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Space deleted"),
        @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteSpace(
            @Parameter(hidden = true) Authentication authentication, @PathVariable Long spaceId) {
        String username = authentication.getName();
        spaceService.delete(username, spaceId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{spaceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates a space", description = "Updates a space name and description by id.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Space updated"),
        @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> updateSpace(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long spaceId,
            @RequestBody @NotNull UpdateSpaceRequestDTO request) {
        String username = authentication.getName();
        spaceService.update(username, spaceId, request.name(), request.description());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{spaceId}/mind-map")
    @Operation(
            summary = "Create a mind-map for the specified space",
            description =
                    """
                    Starts asynchronous mind-map generation for the specified space.
                    The mind-map is created from the documents currently associated with the space.
                    The request is accepted for background processing and the result may not be immediately available.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Mind-map generation accepted"),
        @ApiResponse(responseCode = "400", description = "Invalid request or the space cannot be processed"),
        @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space"),
        @ApiResponse(responseCode = "404", description = "Space not found"),
        @ApiResponse(responseCode = "409", description = "A mind-map generation is already in progress for this space"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MindMap> extractMindMap(
            @Parameter(description = "The ID of the space to create the mind map for", required = true)
                    @jakarta.validation.constraints.NotNull
                    @PathVariable("spaceId")
                    Long spaceId,
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        spaceService.createMindMap(username, spaceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{spaceId}/mind-map")
    @Operation(
            summary = "Get the mind-map for the specified space",
            description =
                    """
                    Returns the current mind-map for the specified space.
                    If no mind-map has been generated yet for the space, the endpoint returns 404.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mind-map retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space"),
        @ApiResponse(responseCode = "404", description = "Space or mind-map not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MindMapResponseDTO> getMindMap(
            @Parameter(description = "The ID of the space to retrieve the mind map for", required = true)
                    @jakarta.validation.constraints.NotNull
                    @PathVariable("spaceId")
                    Long spaceId,
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        var mindMap = spaceService.getMindMap(username, spaceId);
        var dto = MindMapResponseDTO.from(mindMap);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{spaceId}/mind-map/status")
    @Operation(
            summary = "Get the mind-map processing status for the specified space",
            description =
                    """
                    Returns the current processing status of the mind-map for the specified space.
                    The response indicates whether mind-map generation is pending, in progress, completed, or failed.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mind-map status retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space"),
        @ApiResponse(responseCode = "404", description = "Space not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MindMapStatusDTO> getMindMapStatus(
            @Parameter(description = "The ID of the space to retrieve the mind-map status for", required = true)
                    @jakarta.validation.constraints.NotNull
                    @PathVariable("spaceId")
                    Long spaceId,
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var status = spaceService.getMindMapStatus(username, spaceId);
        return ResponseEntity.ok(new MindMapStatusDTO(status));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/{spaceId}/documents/status")
    @Operation(
            summary = "Get document processing statuses for the specified space",
            description =
                    """
                    Returns the processing status of all documents associated with the specified space.
                    Each document status indicates whether processing is pending, in progress, completed, or failed.
                    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document statuses retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space"),
        @ApiResponse(responseCode = "404", description = "Space not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<DocumentStatus>> getDocumentStatuses(
            @Parameter(description = "The ID of the space to retrieve document statuses for", required = true)
                    @jakarta.validation.constraints.NotNull
                    @PathVariable("spaceId")
                    Long spaceId,
            @Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.ok(List.of());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, path = "/{spaceId}/documents")
    @Operation(
            summary = "Upload a document to the specified space",
            description =
                    """
                            Uploads a single document file for asynchronous processing.
                            Supported file types depend on the server-side processing pipeline.
                            The file must be sent as multipart/form-data using the field name 'document'.
                            """)
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Document accepted for processing"),
        @ApiResponse(responseCode = "400", description = "Invalid request or missing file"),
        @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space"),
        @ApiResponse(responseCode = "413", description = "Uploaded file is too large"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<@org.jetbrains.annotations.NotNull Void> createDocument(
            @Parameter(
                            description = "The document file to upload",
                            required = true,
                            content =
                                    @Content(
                                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                            schema = @Schema(type = "string", format = "binary")))
                    @RequestPart("document")
                    @jakarta.validation.constraints.NotNull
                    MultipartFile document,
            @Parameter(description = "The ID of the space to upload the document to", required = true)
                    @jakarta.validation.constraints.NotNull
                    @PathVariable("spaceId")
                    Long spaceId,
            @Parameter(hidden = true) Authentication authentication) {
        if (document.isEmpty() || document.getSize() >= appProperties.getMaxDocumentSize()) {
            return ResponseEntity.badRequest().build();
        }

        String username = authentication.getName();
        documentService.createDocument(document, username, spaceId);
        return ResponseEntity.accepted().build();
    }
}
