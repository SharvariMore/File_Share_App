package in.sharvarimore.fileshareapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

@Component
@RequiredArgsConstructor

public class ClerkJwtAuthFilter extends OncePerRequestFilter {

    @Value("${clerk.issuer}")
    private String clerkIssuer;

    private final ClerkJwksProvider jwksProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // skip JWT validation if URI contains webhooks
        if(request.getRequestURI().contains("/webhooks") ||
                request.getRequestURI().contains("/public") ||
                request.getRequestURI().contains("/download"))
        {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");  // get request from header if not authenticated

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization Header missing/invalid!");
            return;
        }

        // if authheader starts with "Bearer"
        try{
            String token = authHeader.substring(7);  // from index 7 get token
            // split token to get key id
            String[] chunks = token.split("\\.");
            if(chunks.length < 3) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT Token Format!");
                return;
            }

            // if chunk length > 3
            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode headerNode = mapper.readTree(headerJson);

            if (!headerNode.has("kid")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token Header Missing kid!");
                return;
            }

            // if headerNode has kid get it
            String kid = headerNode.get("kid").asString();

            // once received kid pass to publicKey
            PublicKey publicKey = jwksProvider.getPublicKey(kid);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .setAllowedClockSkewSeconds(60)
                    .requireIssuer(clerkIssuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String clerkId = claims.getSubject();

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(clerkId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);   // continue filter and pass req, resp to next filter

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT Token: "+ e.getMessage());
            return;
        }
    }
}
