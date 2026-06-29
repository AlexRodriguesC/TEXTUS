package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DocumentsResponseDTO(@NotNull List<DocumentResponseDTO> documents) {}
