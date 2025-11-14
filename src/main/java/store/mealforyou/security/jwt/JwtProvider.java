package store.mealforyou.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import store.mealforyou.security.MemberDetailService;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    @Value("${app.jwt.issuer}") private String issuer;
    @Value("${app.jwt.secret}") private String secret;
    @Value("${app.jwt.access-exp-min}") private long accessExpMin;
    @Value("${app.jwt.refresh-exp-day}") private long refreshExpDay;

    private final MemberDetailService memberDetailService;
    private Key key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createAccessToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExpMin * 60)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExpDay * 24 * 3600)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public Authentication getAuthentication(String token) {
        var userDeatils = memberDetailService.loadUserByUsername(getEmail(token));
        return new UsernamePasswordAuthenticationToken(userDeatils, null, userDeatils.getAuthorities());
    }
}
