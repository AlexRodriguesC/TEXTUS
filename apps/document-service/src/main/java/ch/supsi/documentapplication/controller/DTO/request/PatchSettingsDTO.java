package ch.supsi.documentapplication.controller.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Valid
@Getter
@Schema(description = "Patch request for updating learning path settings. All fields are optional." +
        "Percentages should sum to 100 for a valid distribution of exercise types.")
public class PatchSettingsDTO {

    @Min(0)
    @Max(100)
    @Schema(
            description = "Percentage of question-type exercises in the path",
            example = "40",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer questionPercentage;

    @Min(0)
    @Max(100)
    @Schema(
            description = "Percentage of list-type exercises in the path",
            example = "30",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer listPercentage;

    @Min(0)
    @Max(100)
    @Schema(
            description = "Percentage of group-type exercises in the path",
            example = "20",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer groupPercentage;

    @Min(0)
    @Max(100)
    @Schema(
            description = "Percentage of pair-type exercises in the path",
            example = "10",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer pairPercentage;
}