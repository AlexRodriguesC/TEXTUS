package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.PathSettings;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response DTO containing the learning path settings with percentage allocations")
public record PathSettingsResponseDTO(
        @NotNull
        @Schema(description = "Percentage of question-type exercises in the learning path", example = "30")
        Integer questionPercentage,

        @NotNull
        @Schema(description = "Percentage of list-type exercises in the learning path", example = "20")
        Integer listPercentage,

        @NotNull
        @Schema(description = "Percentage of pair-type exercises in the learning path", example = "25")
        Integer pairPercentage,

        @NotNull
        @Schema(description = "Percentage of group-type exercises in the learning path", example = "25")
        Integer groupPercentage
) {

    public static PathSettingsResponseDTO from(PathSettings settings) {
        return new PathSettingsResponseDTO(
                settings.getQuestionPercentage(),
                settings.getListPercentage(),
                settings.getPairPercentage(),
                settings.getGroupPercentage()
        );
    }
}
