package com.kh.spring.util;


import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component //spring에서 관리할 수 있도록 컴포넌트화 시키기
public class JWTUtil {
    @Value("${jwt.secret:mySecretkeybackupTokenkey123safsafsafsfsfafsfsfa}")
    private String secret;


    @Value("${jwt.expiration:864000000}")
    private long expiration;


    private SecretKey getSignKey() {

        return Keys.hmacShaKeyFor(secret.getBytes());

    }


    public String generateToken(String userId) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime()+expiration);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSignKey())
                .compact();
    }


    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();


        return claims.getSubject();
    }


//JWT 토큰이 유효한지 검증하는 메소드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;


        }catch(JwtException e){


            e.printStackTrace();


            return false;

        }

    }


// 만약 JWT토큰의 만료여부만 확인하고 싶다면

    public boolean isTokenExpired(String token) {

        try {


            Claims claims = Jwts.parserBuilder()

                    .setSigningKey(getSignKey())

                    .build()

                    .parseClaimsJws(token)

                    .getBody();


            return claims.getExpiration().before(new Date());

        }catch(JwtException e) {


            return false;

        }

    }

}

