package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.response.ErrorResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.MindMapJobDTO;
import ch.supsi.documentapplication.controller.DTO.response.MindMapResponseDTO;
import ch.supsi.documentapplication.model.MindMap;
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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/jobs")
    @Operation(summary = "Get mind-map job status for the specified space", description = """
            Returns the current status of the mind-map
            generation job associated with the specified space.
            """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Job status retrieved"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space or job not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<MindMapJobDTO> getMindMapJob(@Parameter(description = "The ID of the space to retrieve the mind-map job for", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
                                                       @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var status = spaceService.getMindMapStatus(username, spaceId);
        return ResponseEntity.ok(new MindMapJobDTO(status));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a mind-map for the specified space", description = """
            Starts asynchronous mind-map generation for the specified space.
            The mind-map is created from the documents currently associated with the space.
            The request is accepted for background processing and the result may not be immediately available.
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
    public ResponseEntity<MindMap> extractMindMap(@Parameter(description = "The ID of the space to create the mind map for", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
                                                  @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        spaceService.createMindMap(username, spaceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the mind-map for the specified space", description = """
            Returns the current mind-map for the specified space.
            If no mind-map has been generated yet for the space, the endpoint returns 404.
            """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Mind-map retrieved successfully"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<MindMapResponseDTO> getMindMap(@Parameter(description = "The ID of the space to retrieve the mind map for", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
                                                         @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        var mindMap = spaceService.getMindMap(username, spaceId);
        var dto = MindMapResponseDTO.from(mindMap);
        return ResponseEntity.ok(dto);
    }
}
