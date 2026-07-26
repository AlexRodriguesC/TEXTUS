package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.List;

@Valid
public record ModelsResponseDTO(@NotNull List<ModelResponseDTO> models) {
}
