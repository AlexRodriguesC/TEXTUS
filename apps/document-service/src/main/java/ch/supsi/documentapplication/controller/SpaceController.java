package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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
                    CreateSpaceRequestDTO createSpaceRequestDTO) {

        Long id = spaceService.create(createSpaceRequestDTO.getName(), createSpaceRequestDTO.getDescription());
        return ResponseEntity.ok(new CreateSpaceResponseDTO(id));
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
        @ApiResponse(responseCode = "413", description = "Uploaded file is too large"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<@org.jetbrains.annotations.NotNull Void> uploadDocument(
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
                    Long spaceId) {
        if (document.isEmpty() || document.getSize() >= appProperties.getMaxDocumentSize()) {
            return ResponseEntity.badRequest().build();
        }

        documentService.createDocument(document, spaceId);
        return ResponseEntity.accepted().build();
    }
}
