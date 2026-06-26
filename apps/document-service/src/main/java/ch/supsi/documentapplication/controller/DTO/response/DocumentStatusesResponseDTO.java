package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.DocumentStatus;

import java.util.List;

public record DocumentStatusesResponseDTO(List<DocumentStatus> statuses) {}
