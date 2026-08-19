package ch.supsi.documentapplication.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {

    @NotNull()
    @NotEmpty()
    String username;

    @NotNull()
    @NotEmpty()
    String password;
}
