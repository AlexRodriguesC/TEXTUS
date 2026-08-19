package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.mapper.DocumentTaskMapper;
import ch.supsi.documentapplication.controller.DTO.request.MoveDocumentRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.DocumentResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.DocumentsResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.ErrorResponseDTO;
import ch.supsi.documentapplication.configuration.properties.AppProperties;
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

@RestController
@RequestMapping("api/v1/spaces/{spaceId}/documents")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Documents", description = "Endpoints for handling documents within a space")
public class DocumentController {

    private final AppProperties appProperties;
    private final IDocumentService documentService;
    private final DocumentTaskMapper documentTaskMapper;

    @Autowired
    public DocumentController(
            AppProperties appProperties,
            IDocumentService documentService,
            DocumentTaskMapper documentTaskMapper) {
        this.appProperties = appProperties;
        this.documentService = documentService;
        this.documentTaskMapper = documentTaskMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get documents for the specified space", description = """
            Returns all documents associated with the specified space.
            Each document representation contains the document id and its pipeline task progress.
            """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<DocumentsResponseDTO> getDocuments(
            @Parameter(description = "The ID of the space to retrieve documents for", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
            @Parameter(hidden = true) Authentication authentication) {
        String owner = authentication.getName();
        var documents = documentService.getDocuments(owner, spaceId);
        var documentsDto = documents.stream()
                .map(document -> new DocumentResponseDTO(
                        document.getName(),
                        document.getId(),
                        document.getOrderIndex(),
                        documentTaskMapper.toDtoList(document.getTasks())))
                .toList();
        return ResponseEntity.ok(new DocumentsResponseDTO(documentsDto));
    }

    @PatchMapping(value = "/{documentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Move a document within its space")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "204", description = "Document moved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid document order index", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Document or space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<Void> moveDocument(
            @Parameter(description = "The ID of the space containing the document", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
            @Parameter(description = "Unique identifier of the document to move", required = true) @PathVariable("documentId") Long documentId,
            @Parameter(hidden = true) Authentication authentication,
            @jakarta.validation.Valid @RequestBody @NotNull MoveDocumentRequestDTO request) {
        documentService.moveDocument(authentication.getName(), spaceId, documentId, request.orderIndex());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{documentId}/retry")
    @Operation(summary = "Retry a failed document processing job", description = """
            Restarts the document processing pipeline from the last checkpoint.
            Only documents in a failed state can be retried.
            """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "202", description = "Document processing retry accepted"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Document or space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "409", description = "Document is not in a retryable state", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<@NotNull Void> retryDocument(
            @Parameter(description = "The ID of the space containing the document", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
            @Parameter(description = "Unique identifier of the document to retry", required = true) @PathVariable("documentId") Long documentId,
            @Parameter(hidden = true) Authentication authentication) {
        String owner = authentication.getName();
        documentService.retryDocument(owner, spaceId, documentId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document to the specified space", description = """
            Uploads a single document file for asynchronous processing.
            Supported file types depend on the server-side processing pipeline.
            The file must be sent as multipart/form-data using the field name 'document'.
            """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "202", description = "Document accepted for processing"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or missing file", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "413", description = "Uploaded file is too large", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "415", description = "Unsupported media type", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<@NotNull Void> createDocument(
            @Parameter(description = "The document file to upload", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary"))) @RequestPart("document") @NotNull MultipartFile fsDocument,
            @Parameter(description = "The ID of the space to upload the document to", required = true) @NotNull @PathVariable("spaceId") Long spaceId, @Parameter(hidden = true) Authentication authentication) {
        if (fsDocument.isEmpty() || fsDocument.getSize() >= appProperties.getMaxDocumentSize()) {
            return ResponseEntity.badRequest().build();
        }

        String username = authentication.getName();
        documentService.createDocument(fsDocument, username, spaceId);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete a document by ID", description = "Permanently deletes the document and all associated information.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "204", description = "Document successfully deleted"),
                    @ApiResponse(responseCode = "403", description = "User does not have permission to delete this document", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Document or space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "409", description = "Document cannot be deleted because a mind map or path was already generated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<Void> deleteDocument(@Parameter(description = "The ID of the space to delete the document from", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
                                               @Parameter(description = "Unique identifier of the document to delete", required = true) @PathVariable("documentId") Long documentId,
                                               @Parameter(hidden = true) Authentication authentication) {
        String owner = authentication.getName();
        documentService.deleteDocument(owner, spaceId, documentId);
        return ResponseEntity.noContent().build();
    }
}
