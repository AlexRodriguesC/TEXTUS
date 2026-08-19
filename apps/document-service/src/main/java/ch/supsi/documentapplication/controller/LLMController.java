package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.mapper.ModelMapper;
import ch.supsi.documentapplication.controller.DTO.response.ErrorResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.ModelsResponseDTO;
import ch.supsi.documentapplication.model.RawModel;
import ch.supsi.documentapplication.service.InferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/models")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Models", description = "Endpoints for retrieving information about LLMs")
public class LLMController {

    private final InferenceService inferenceService;
    private final ModelMapper modelMapper;

    @Autowired
    public LLMController(InferenceService inferenceService, ModelMapper modelMapper) {
        this.inferenceService = inferenceService;
        this.modelMapper = modelMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get available LLM models", description = """
        Returns the list of language models currently available for use.
        Each model representation contains its identifier, name, description, pricing, context length and capability flags.
        """)
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Models retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error or provider unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<ModelsResponseDTO> getModels() {
        List<RawModel> rawModels = inferenceService.getAvailableModels();
        return ResponseEntity.ok(modelMapper.toDTO(rawModels));
    }

}
