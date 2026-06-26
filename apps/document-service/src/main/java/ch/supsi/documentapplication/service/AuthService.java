package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseUserStorage;
import ch.supsi.documentapplication.exception.UsernameTakenException;
import ch.supsi.documentapplication.model.User;
import ch.supsi.documentapplication.properties.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    private final String secretKey;
    private final IDatabaseUserStorage databaseUserStorage;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(
            AppProperties appProperties, IDatabaseUserStorage databaseUserStorage, PasswordEncoder passwordEncoder) {
        this.secretKey = appProperties.getSecretKey();
        this.databaseUserStorage = databaseUserStorage;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(User user) {
        if (databaseUserStorage.readUser(user.getUsername()).isPresent()) {
            throw new UsernameTakenException("Username is already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        databaseUserStorage.createUser(user);
        return generateToken(user.getUsername());
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public @NotNull UserDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {
        Optional<User> optUser = databaseUserStorage.readUser(username);
        if (optUser.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        var user = optUser.get();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}
