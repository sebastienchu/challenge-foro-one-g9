package alura.blog.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    private static final String SECRET = "secret-key"; // Mejor usar variable de entorno
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24h

    // Genera un token JWT a partir del email del usuario
    public String generateTokenByEmail(String email) {
        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION))
                .sign(Algorithm.HMAC256(SECRET));
    }

    // Obtiene el email (subject) desde un token
    public String getSubject(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token)
                .getSubject();
    }
}

