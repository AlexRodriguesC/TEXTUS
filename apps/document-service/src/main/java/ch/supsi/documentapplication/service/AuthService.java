package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseUserStorage;
import ch.supsi.documentapplication.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final IDatabaseUserStorage databaseUserStorage;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(IDatabaseUserStorage databaseUserStorage, PasswordEncoder passwordEncoder) {
        this.databaseUserStorage = databaseUserStorage;
        this.passwordEncoder = passwordEncoder;
    }

    private final String SECRET_KEY = "secretkey123secretkey123secretkey123"; // min 32 chars for HS256

    public String register(User user) {
        if (databaseUserStorage.readUser(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        databaseUserStorage.createUser(user);
        return generateToken(user.getUsername());
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
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
}
