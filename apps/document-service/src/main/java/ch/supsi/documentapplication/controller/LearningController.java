package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.response.ErrorResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.LearningPathJobDTO;
import ch.supsi.documentapplication.service.ILearningService;
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
@RequestMapping("api/v1/spaces/{spaceId}/paths")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Paths", description = "Endpoints for handling learning paths within a space")
public class LearningController {

    private final ILearningService learningService;

    @Autowired
    public LearningController(ILearningService learningService) {
        this.learningService = learningService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generate a learning path for the specified space", description = """
            Starts asynchronous learning-path generation for the specified space.
            The learning path is created from the mind-map topics currently associated with the space.
            The request is accepted for background processing and the result may not be immediately available.
            """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "202", description = "Learning path generation accepted"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or the space cannot be processed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "409", description = "A learning-path generation is already in progress for this space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<Void> extractLearningPath(@Parameter(description = "The ID of the space to generate the learning path for", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
                                                    @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        learningService.createLearningPath(username, spaceId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/jobs")
    @Operation(summary = "Get learning-path job status for the specified space", description = """
        Returns the current status of the learning-path
        generation job associated with the specified space.
        """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Job status retrieved"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space or job not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<LearningPathJobDTO> getLearningPathJob(@Parameter(description = "The ID of the space to retrieve the learning-path job for", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
                                                                 @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var status = learningService.getLearningPathStatus(username, spaceId);
        return ResponseEntity.ok(new LearningPathJobDTO(status));
    }

}
