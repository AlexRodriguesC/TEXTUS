package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.response.*;
import ch.supsi.documentapplication.controller.DTO.response.exercise.*;
import ch.supsi.documentapplication.model.learn.Lesson;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;
import ch.supsi.documentapplication.service.ILearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/spaces/{spaceId}/lessons/{lessonId}")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Lessons", description = "Endpoints for handling learning path lessons and their associated exercises within a space")
public class LearningController {

    private final ILearningService learningService;
    private final ExerciseMapper exerciseMapper;

    @Autowired
    public LearningController(ILearningService learningService, ExerciseMapper exerciseMapper) {
        this.learningService = learningService;
        this.exerciseMapper = exerciseMapper;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Start or resume the lesson", description = """
        Starts a new lesson, or resumes the existing in-progress
        lesson if one already exists for the current user. Triggered when the user
        clicks a lesson on their learning path.
        """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "New lesson created"),
            @ApiResponse(responseCode = "409", description = "Existing lesson already in progress"),
            @ApiResponse(responseCode = "404", description = "Space or lesson not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LessonResponseDTO> startLesson(
            @PathVariable Long spaceId,
            @PathVariable Long lessonId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String owner = authentication.getName();
        Lesson lesson = learningService.getLesson(owner, spaceId, lessonId);

        return lesson != null
                ? ResponseEntity.status(HttpStatus.CREATED).body(LessonResponseDTO.from(lesson))
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get lesson status and mastery", description = """
        Returns the current status of the lesson associated with this lesson, including
        the user's mastery probability as computed by Bayesian Knowledge Tracing (BKT),
        the number of attempts used so far, and whether the lesson has been completed
        (mastered or exhausted).
        """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lesson not found for this lesson", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LessonResponseDTO> getLesson(
            @PathVariable Long spaceId,
            @PathVariable Long lessonId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String owner = authentication.getName();
        Lesson lesson = learningService.getLesson(owner, spaceId, lessonId);

        return ResponseEntity.ok(LessonResponseDTO.from(lesson));
    }

    @GetMapping(value = "/exercise", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the next exercise for the lesson", description = """
    Returns the next not-yet-completed exercise for this lesson's lesson, selected
    dynamically based on BKT mastery and exercise family. Returns 204 No Content
    once no further exercises are needed for this lesson.
    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Next exercise returned successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(oneOf = {
                                    QuestionResponseDTO.class,
                                    ListResponseDTO.class,
                                    PairResponseDTO.class,
                                    GroupResponseDTO.class
                            }))),
            @ApiResponse(responseCode = "204", description = "No further exercises to serve; lesson is complete"),
            @ApiResponse(responseCode = "404", description = "Lesson not found for this lesson", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<ExerciseDTO> getNextExercise(
            @PathVariable Long spaceId,
            @PathVariable Long lessonId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String owner = authentication.getName();
        BaseExercise<?> exercise = learningService.getNextExercise(owner, spaceId, lessonId);
        return ResponseEntity.ok(exerciseMapper.toDto(exercise));
    }

    @GetMapping(value = "/lesson/exercises", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all exercises served for this lesson", description = """
        Returns the full list of exercises served so far for this lesson's lesson, each
        with its resolved attempt status. Useful for review or debugging; not part of
        the primary lesson-taking flow.
        """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lesson not found for this lesson", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<ExercisesResponseDTO> getExercises(
            @PathVariable Long spaceId,
            @PathVariable Long lessonId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String owner = authentication.getName();
        var exercises = learningService.getExercises(owner, spaceId, lessonId);
        return ResponseEntity.ok(new ExercisesResponseDTO(exercises.stream().map(ExerciseResponseDTO::from).toList()));
    }

    @PostMapping(value = "/lesson/exercise/attempts", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Submit an attempt for the lesson's current exercise", description = """
        Submits the user's answer for the currently active exercise of this lesson's lesson.
        The backend validates the submitted exerciseId against the lesson's current exercise,
        grades the responses, updates BKT mastery, and returns per-item feedback along with
        the updated lesson state. The attemptId should be client-generated to support safe
        retries without creating duplicate attempts.
        """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attempt recorded and feedback returned"),
            @ApiResponse(responseCode = "409", description = "Submitted exerciseId does not match the lesson's current exercise", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Lesson not found for this lesson", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<AttemptResponseDTO> submitAttempt(
            @PathVariable Long spaceId,
            @PathVariable Long lessonId,
            @RequestBody AttemptRequestDTO request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        String owner = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(learningService.submitAttempt(owner, spaceId, lessonId, request));
    }
}