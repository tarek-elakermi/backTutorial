package com.example.demo.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.impl.JWTParser;
import com.sun.security.auth.UserPrincipal;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;

import java.util.Date;


public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @Value("${backTutorial.app.jwtSecret}")
    private String jwtSecret;
    @Value("${backTutorial.app.jwtExpiration}")
    private int jwtExpiration;

    public String generateJwtToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getName())) // we want to change it to userPrincipal.getUsername()!!!
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() +
                        jwtExpiration*1000))
                .signWith(SignatureAlgorithm.ES512, jwtSecret)
                .compact();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature -> Message : {} ", e );
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT signature -> Message : {} ", e );
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT signature -> Message : {} ", e );
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT signature -> Message : {} ", e );
        } catch ( IllegalArgumentException e) {
            logger.error("JWT claims string is empty -> Message : {} ", e );
        }
        return false;
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

}
