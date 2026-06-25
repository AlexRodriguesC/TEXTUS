package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.response.TokenResponseDTO;
import ch.supsi.documentapplication.model.User;
import ch.supsi.documentapplication.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for handling authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<@NotNull TokenResponseDTO> login(@RequestBody User user) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        return ResponseEntity.ok(new TokenResponseDTO(authService.generateToken(user.getUsername())));
    }

    @PostMapping("/register")
    public ResponseEntity<@NotNull TokenResponseDTO> register(@RequestBody User user) {
        return ResponseEntity.ok(new TokenResponseDTO(authService.register(user)));
    }
}
