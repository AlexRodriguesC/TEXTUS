package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TopicResponseDTO(
        @NotNull Long id,
        @NotNull @NotEmpty String title,
        @NotNull @NotEmpty String summary,
        @NotNull @NotEmpty List<String> labels) {}
