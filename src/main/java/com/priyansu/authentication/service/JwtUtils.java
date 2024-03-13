package com.priyansu.authentication.service;

import com.priyansu.authentication.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

@Component
public class JwtUtils {

    private SecretKey SECRET_KEY;
    private static final long EXPIRATION_TIME = 24*60*60*1000L;

    public JwtUtils(){
        String secret_string = "sbdhfbwjhfnudhdiasdskjcnudjfhuidgayuisbgvifsvdfakbvauihhcusdaihuadsinh";
        byte[] key_bytes = Base64.getDecoder().decode(secret_string.getBytes(StandardCharsets.UTF_8));
        this.SECRET_KEY = new SecretKeySpec(key_bytes,"HmacSHA256");
    }
    public String genrateToken(UserDetails userDetails){
        return Jwts.builder().subject(userDetails.getUsername()).issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
                .signWith(SECRET_KEY).compact();
    }

    public String genrateRefreashToken(HashMap<String, Object> claims,UserDetails userDetails){
        return Jwts.builder().claims(claims).subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
                .signWith(SECRET_KEY).compact();

    }
    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }
    private <T> T extractClaims(String token, Function<Claims,T> claimsTFunction){
        return claimsTFunction.apply(Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload());
    }

    public boolean isTokenValid(String token,UserDetails userDetails){
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token){
        return extractClaims(token,Claims::getExpiration).before(new Date());
    }

    public String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
