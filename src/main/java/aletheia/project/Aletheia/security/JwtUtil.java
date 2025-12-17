package aletheia.project.Aletheia.security;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private JwtProperties properties;
    private SecretKey key;

    public JwtUtil(JwtProperties properties){
        if(properties.getSecret() == null){
            throw new IllegalArgumentException("JWT secret is missing! Check application.properties");
        }
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.getSecret()));
    }

    public String generateToken(String username){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExp());
        return Jwts.builder().subject(username).issuedAt(new Date()).expiration(expiry).signWith(key).compact();
    }

    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
