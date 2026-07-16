package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/spaces/{spaceId}/nodes/{nodeId}")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Nodes", description = "Endpoints for handling learning path nodes and their associated exercises within a space")
public class LearningController {

    // ---------------------------------------------------------------------
    // Lesson lifecycle
    // ---------------------------------------------------------------------

    @PostMapping(value = "/lesson", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start or resume the lesson for a node", description = """
        Starts a new lesson for the given node, or resumes the existing in-progress
        lesson if one already exists for the current user. Triggered when the user
        clicks a node on their learning path.
        """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "New lesson created"),
            @ApiResponse(responseCode = "200", description = "Existing in-progress lesson resumed"),
            @ApiResponse(responseCode = "404", description = "Space or node not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LessonResponseDTO> startLesson(
            @PathVariable Long spaceId,
            @PathVariable Long nodeId
    ) {
        LessonResponseDTO lesson = null; // service call goes here
        boolean wasCreated = true; // determined by service
        return wasCreated
                ? ResponseEntity.status(HttpStatus.CREATED).body(lesson)
                : ResponseEntity.ok(lesson);
    }

    @GetMapping(value = "/lesson", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get lesson status and mastery", description = """
        Returns the current status of the lesson associated with this node, including
        the user's mastery probability as computed by Bayesian Knowledge Tracing (BKT),
        the number of attempts used so far, and whether the lesson has been completed
        (mastered or exhausted).
        """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lesson not found for this node", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LessonResponseDTO> getLesson(
            @PathVariable Long spaceId,
            @PathVariable Long nodeId
    ) {
        return ResponseEntity.ok(null); // service call goes here
    }

    // ---------------------------------------------------------------------
    // Exercises
    // ---------------------------------------------------------------------

    @GetMapping(value = "/lesson/exercise", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the next exercise for the lesson", description = """
        Returns the next not-yet-completed exercise for this node's lesson, selected
        dynamically based on BKT mastery and exercise family. Returns 204 No Content
        once no further exercises are needed for this lesson.
        """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Next exercise returned successfully"),
            @ApiResponse(responseCode = "204", description = "No further exercises to serve; lesson is complete"),
            @ApiResponse(responseCode = "404", description = "Lesson not found for this node", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<ExerciseResponseDTO> getNextExercise(
            @PathVariable Long spaceId,
            @PathVariable Long nodeId
    ) {
        return ResponseEntity.ok(null); // service call goes here
    }

    @GetMapping(value = "/lesson/exercises", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all exercises served for this lesson", description = """
        Returns the full list of exercises served so far for this node's lesson, each
        with its resolved attempt status. Useful for review or debugging; not part of
        the primary lesson-taking flow.
        """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lesson not found for this node", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<List<ExerciseResponseDTO>> getExercises(
            @PathVariable Long spaceId,
            @PathVariable Long nodeId
    ) {
        return ResponseEntity.ok(null); // service call goes here
    }

    // ---------------------------------------------------------------------
    // Attempts
    // ---------------------------------------------------------------------

    @PostMapping(value = "/lesson/exercise/attempts", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Submit an attempt for the lesson's current exercise", description = """
        Submits the user's answer for the currently active exercise of this node's lesson.
        The backend validates the submitted exerciseId against the lesson's current exercise,
        grades the responses, updates BKT mastery, and returns per-item feedback along with
        the updated lesson state. The attemptId should be client-generated to support safe
        retries without creating duplicate attempts.
        """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attempt recorded and feedback returned"),
            @ApiResponse(responseCode = "409", description = "Submitted exerciseId does not match the lesson's current exercise", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Lesson not found for this node", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<AttemptResponseDTO> submitAttempt(
            @PathVariable Long spaceId,
            @PathVariable Long nodeId,
            @RequestBody AttemptRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(null); // service call goes here
    }
}