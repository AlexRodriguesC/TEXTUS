package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.properties.AppProperties;
import ch.supsi.documentapplication.service.IDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/documents")
@Tag(name = "Documents", description = "Endpoints for uploading and processing documents")
public class DocumentController {

    private final IDocumentService documentService;
    private final AppProperties appProperties;

    @Autowired
    public DocumentController(IDocumentService documentService, AppProperties appProperties) {
        this.documentService = documentService;
        this.appProperties = appProperties;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a document",
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
    public ResponseEntity<Void> uploadDocument(
            @Parameter(
                            description = "The document file to upload",
                            required = true,
                            content =
                                    @Content(
                                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                            schema = @Schema(type = "string", format = "binary")))
                    @RequestPart("document")
                    @NotNull
                    MultipartFile document) {
        if (document.isEmpty() || document.getSize() >= appProperties.getMaxDocumentSize()) {
            return ResponseEntity.badRequest().build();
        }

        documentService.parseDocument(document);
        return ResponseEntity.accepted().build();
    }
}
