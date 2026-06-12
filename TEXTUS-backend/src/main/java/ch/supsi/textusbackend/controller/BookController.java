package ch.supsi.textusbackend.controller;

import ch.supsi.textusbackend.service.IBookService;
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
@RequestMapping("api/v1/books")
@Tag(name = "Books", description = "Endpoints for uploading and processing books")
public class BookController {

    private final IBookService bookService;

    @Autowired
    public BookController(IBookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a book",
            description =
                    """
                            Uploads a single book file for asynchronous processing.
                            Supported file types depend on the server-side processing pipeline.
                            The file must be sent as multipart/form-data using the field name 'book'.
                            """)
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Book accepted for processing"),
        @ApiResponse(responseCode = "400", description = "Invalid request or missing file"),
        @ApiResponse(responseCode = "413", description = "Uploaded file is too large"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> uploadBook(
            @Parameter(
                            description = "The book file to upload",
                            required = true,
                            content =
                                    @Content(
                                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                            schema = @Schema(type = "string", format = "binary")))
                    @RequestPart("book")
                    @NotNull
                    MultipartFile book) {
        if (book.isEmpty() || book.getSize() >= 500L * 1024 * 1024) {
            return ResponseEntity.badRequest().build();
        }

        bookService.createBook(book);
        return ResponseEntity.accepted().build();
    }
}
