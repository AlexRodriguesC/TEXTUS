package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.PathSettings;
import jakarta.validation.constraints.NotNull;

public record PathSettingsResponseDTO(@NotNull Integer questionPercentage, @NotNull Integer listPercentage, @NotNull Integer pairPercentage, @NotNull Integer groupPercentage) {

    public static PathSettingsResponseDTO from(PathSettings settings) {
        return new PathSettingsResponseDTO(
                settings.getQuestionPercentage(),
                settings.getListPercentage(),
                settings.getPairPercentage(),
                settings.getGroupPercentage()
        );
    }
}
