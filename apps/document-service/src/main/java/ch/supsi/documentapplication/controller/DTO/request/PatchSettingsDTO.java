package ch.supsi.documentapplication.controller.DTO.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Valid
@Getter
public class PatchSettingsDTO {

    @Min(0)
    @Max(100)
    Integer questionPercentage;

    @Min(0)
    @Max(100)
    Integer listPercentage;

    @Min(0)
    @Max(100)
    Integer groupPercentage;

    @Min(0)
    @Max(100)
    Integer pairPercentage;
}
