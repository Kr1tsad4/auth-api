package app.auth.security;

import app.auth.dtos.ErrorResponseDto;
import app.auth.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public JwtAuthenticationFilter(UserService userService, JwtUtil jwtUtil) {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    private void writeError(HttpServletResponse response, int status, String message, String path) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String error = HttpStatus.valueOf(status).getReasonPhrase();

        ErrorResponseDto errorResponse = new ErrorResponseDto(status, error, message, path);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST
                    , "No access token"
                    , request.getServletPath());
            return;
        }

        String token = authHeader.substring(7);
        try {
            Map<String, Object> claims = jwtUtil.getJWTClaimsSet(token);

            jwtUtil.verifyToken(token);

            if (!jwtUtil.isValidClaims(claims)) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED
                        , "Invalid token", request.getServletPath());
                return;
            }

            int userId = ((Number) claims.get("id")).intValue();
            String role = (String) claims.get("role");
            Object user = userService.findById(userId);

            List<SimpleGrantedAuthority> authorities = Collections
                    .singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage(), request.getServletPath());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
