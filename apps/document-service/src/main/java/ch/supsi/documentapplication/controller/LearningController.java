package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.request.PatchSettingsDTO;
import ch.supsi.documentapplication.controller.DTO.response.*;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.model.PathSettings;
import ch.supsi.documentapplication.service.ILearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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


    @PutMapping(value = "/settings", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update learning path settings", description = """
            Updates the distribution percentages for question, list, group, and pair content types.
            All values must be between 0 and 100, and their sum must equal 100.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Settings updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input, values out of range or sum not 100", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<?> updatePathSettings(
            @PathVariable("spaceId") @NotNull Long spaceId,
            @Valid @RequestBody PatchSettingsDTO settings,
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        learningService.updateSettings(username, spaceId, PathSettings.from(settings));
        return ResponseEntity.ok().build();
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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the learning path for the specified space", description = """
        Returns the learning path currently generated for the specified space,
        including its chapters and associated topics.
        """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Learning path retrieved"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space or learning path not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<LearningPathResponseDTO> getLearningPath(@Parameter(description = "The ID of the space to retrieve the learning path for", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
                                                                   @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var learningPath = learningService.getLearningPath(username, spaceId);

        if (learningPath == null) {
            throw NotFoundException.learningPathNotFound(spaceId);
        }

        return ResponseEntity.ok(LearningPathResponseDTO.from(learningPath));
    }

    @DeleteMapping
    @Operation(summary = "Delete the learning path for the specified space", description = """
        Deletes the learning path associated with the specified space.
        If a generation job is currently running for this path, it will be stopped first.
        """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "204", description = "Learning path deleted"),
                    @ApiResponse(responseCode = "403", description = "User is not the owner of the specified space", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Space or learning path not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<Void> deleteLearningPath(@Parameter(description = "The ID of the space to delete the learning path for", required = true) @NotNull @PathVariable("spaceId") Long spaceId,
                                                   @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        learningService.deleteLearningPath(username, spaceId);
        return ResponseEntity.noContent().build();
    }

}
