package valleysoft.minibasket.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import valleysoft.minibasket.common.exception.ApiProblems;
import valleysoft.minibasket.common.exception.ErrorCode;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class SecurityErrorHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ApiProblems problems;
    private final JsonMapper jsonMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        unauthorized(request, response, "Bearer");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        write(request, response, ErrorCode.FORBIDDEN);
    }

    void unauthorized(HttpServletRequest request, HttpServletResponse response, String wwwAuthenticate) throws IOException {
        response.setHeader("WWW-Authenticate", wwwAuthenticate);
        write(request, response, ErrorCode.UNAUTHORIZED);
    }

    private void write(HttpServletRequest request, HttpServletResponse response, ErrorCode code) throws IOException {
        ProblemDetail problem = problems.create(code, code.render(Map.of()), Map.of(), request.getRequestURI());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", problem.getType() == null ? "about:blank" : problem.getType().toString());
        body.put("title", problem.getTitle());
        body.put("status", problem.getStatus());
        body.put("detail", problem.getDetail());
        body.put("instance", problem.getInstance().toString());
        body.putAll(problem.getProperties());

        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        jsonMapper.writeValue(response.getWriter(), body);
    }
}
