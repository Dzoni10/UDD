package com.forensicintelligencethreatreport.forensicintelligencethreatreport.auth;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.model.Role;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "secret_password_for_encoding_messages";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(int userId, Role role) {

        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 24*60*60*1000;  //24h traje token

        return Jwts.builder()
                .claim("userId",userId)
                .claim("role",role.name())
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(expMillis))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}