package com.company.skillplatform.auth.security;

import com.company.skillplatform.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {


        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }


        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        try {

            Claims claims = jwtService.parseClaims(token);


            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);

            @SuppressWarnings("unchecked")
            Set<String> roles = Set.copyOf((Collection<String>) claims.get("roles"));

            UserPrincipal principal = new UserPrincipal(userId, email, roles);

            //  Create Authentication
            var auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );

            //  Attach request details (ip, session id,...)
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //  Store in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException | IllegalArgumentException ex) {
            // Token invalid/expired/claims malformed -> don't authenticate
            // Let SecurityConfig handle 401 via AuthenticationEntryPoint if endpoint requires auth
            log.debug("JWT rejected | path={} | reason={}",
                    request.getRequestURI(),
                    ex.getMessage()
            );
        }

        // Continue filter chain
        chain.doFilter(request, response);
    }
}
