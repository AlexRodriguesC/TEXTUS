package ch.supsi.documentapplication.controller.DTO.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Valid
public class CreateSpaceRequestDTO {

    @NotNull
    @NotEmpty
    private String icon;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @NotEmpty
    private String description;
}
