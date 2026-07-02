package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record JobIdDTO(@NotNull @NotEmpty String jobId, @NotNull Long resourceId) {
}
