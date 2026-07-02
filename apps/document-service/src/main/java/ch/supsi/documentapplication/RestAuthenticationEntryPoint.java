package ch.supsi.documentapplication;

import ch.supsi.documentapplication.controller.DTO.response.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

// Handles authentication exceptions thrown during the auth filter chain
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var error = new ErrorResponseDTO(authException.getMessage());
        new ObjectMapper().writeValue(response.getWriter(), error);
    }
}
