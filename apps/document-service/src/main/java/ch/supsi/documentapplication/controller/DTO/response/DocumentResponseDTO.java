package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.DocumentStatus;

public record DocumentResponseDTO(Long id, DocumentStatus status) {}
