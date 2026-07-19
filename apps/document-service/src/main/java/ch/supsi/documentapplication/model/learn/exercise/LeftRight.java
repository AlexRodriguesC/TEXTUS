package ch.supsi.documentapplication.model.learn.exercise;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record LeftRight(@NotNull @NotEmpty String left, @NotNull @NotEmpty String right) {}
