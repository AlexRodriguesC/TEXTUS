package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.response.ErrorResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.PageResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.PagesResponseDTO;
import ch.supsi.documentapplication.service.IPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/spaces/{spaceId}/pages")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pages", description = "Endpoints for handling pages within a space")
public class PageController {

    private final IPageService pageService;

    @Autowired
    public PageController(IPageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get pages for a space",
            description = "Retrieves all pages belonging to the specified space. Only the owner of the space can access its pages.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Pages retrieved successfully",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = PagesResponseDTO.class))),
        @ApiResponse(
                responseCode = "403",
                description = "User is not the owner of the specified space",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Space not found",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<PagesResponseDTO> getPages(
            @Parameter(description = "The ID of the space to retrieve pages from", required = true, example = "1")
                    @PathVariable
                    Long spaceId,
            @Parameter(hidden = true) Authentication authentication) {
        String owner = authentication.getName();

        var pages = pageService.getPagesBySpace(owner, spaceId);
        var dtos = pages.stream()
                .map(p -> new PageResponseDTO(p.getId(), p.getDocumentId(), p.getTitle(), p.getSummary()))
                .toList();

        return ResponseEntity.ok(new PagesResponseDTO(dtos));
    }
}
