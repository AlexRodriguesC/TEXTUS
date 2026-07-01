package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.response.ErrorResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.TopicResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.TopicsResponseDTO;
import ch.supsi.documentapplication.service.ITopicService;
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
@RequestMapping("api/v1/spaces/{spaceId}/topics")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Topics", description = "Endpoints for handling topics within a space")
public class TopicController {

    private final ITopicService topicService;

    @Autowired
    public TopicController(ITopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get topics for a space",
            description =
                    "Retrieves all topics belonging to the specified space. Only the owner of the space can access its topics.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Topics retrieved successfully",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = TopicsResponseDTO.class))),
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
    public ResponseEntity<TopicsResponseDTO> getTopics(
            @Parameter(description = "The ID of the space to retrieve topics from", required = true, example = "1")
                    @PathVariable
                    Long spaceId,
            @Parameter(hidden = true) Authentication authentication) {
        String owner = authentication.getName();

        var topics = topicService.getTopicsBySpace(owner, spaceId);
        var dtos = topics.stream()
                .map(t -> new TopicResponseDTO(t.getId(), t.getTitle(), t.getSummary(), t.getLabels()))
                .toList();

        return ResponseEntity.ok(new TopicsResponseDTO(dtos));
    }
}
