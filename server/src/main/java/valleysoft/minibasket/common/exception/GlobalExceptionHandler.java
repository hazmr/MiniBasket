package valleysoft.minibasket.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Set<String> HIDDEN_CONSTRAINT_ATTRIBUTES = Set.of("message", "groups", "payload");

    private final ApiProblems problems;

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex, HttpServletRequest request) {
        return problems.create(ex.getErrorCode(), ex.getMessage(), ex.getParams(), request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return simple(ErrorCode.FORBIDDEN, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return simple(ErrorCode.DATA_CONFLICT, request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(OptimisticLockingFailureException ex, HttpServletRequest request) {
        return simple(ErrorCode.CONCURRENT_UPDATE, request);
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, QueryTimeoutException.class})
    public ProblemDetail handleUnavailable(RuntimeException ex, HttpServletRequest request) {
        log.error("Backing service unavailable at {}: {}", request.getRequestURI(), ex.getMessage());
        return simple(ErrorCode.SERVICE_UNAVAILABLE, request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleBadSort(PropertyReferenceException ex, HttpServletRequest request) {
        Map<String, Object> params = Map.of("property", ex.getPropertyName());
        return problems.create(ErrorCode.INVALID_SORT, ErrorCode.INVALID_SORT.render(params), params, request.getRequestURI());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = problems.create(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.render(Map.of()),
                Map.of(), pathOf(request));
        problem.setProperty("violations", ex.getBindingResult().getFieldErrors().stream().map(this::violation).toList());
        return ResponseEntity.badRequest().body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            problems.addContract(problem, ErrorCode.forHttpStatus(statusCode.value()));
        }
        return response;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}", request.getRequestURI(), ex);
        return simple(ErrorCode.INTERNAL_ERROR, request);
    }

    private ProblemDetail simple(ErrorCode code, HttpServletRequest request) {
        return problems.create(code, code.render(Map.of()), Map.of(), request.getRequestURI());
    }

    private Map<String, Object> violation(FieldError error) {
        Map<String, Object> violation = new LinkedHashMap<>();
        violation.put("field", error.getField());
        violation.put("code", error.getCode());
        Map<String, Object> params = constraintParams(error);
        if (!params.isEmpty()) {
            violation.put("params", params);
        }
        violation.put("message", error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage());
        return violation;
    }

    private static Map<String, Object> constraintParams(FieldError error) {
        if (!error.contains(ConstraintViolation.class)) {
            return Map.of();
        }
        Map<String, Object> attributes = error.unwrap(ConstraintViolation.class).getConstraintDescriptor().getAttributes();
        Map<String, Object> params = new LinkedHashMap<>();
        attributes.forEach((name, value) -> {
            boolean simpleValue = value instanceof Number || value instanceof String || value instanceof Boolean;
            if (simpleValue && !HIDDEN_CONSTRAINT_ATTRIBUTES.contains(name)) {
                params.put(name, value);
            }
        });
        return params;
    }

    private static String pathOf(WebRequest request) {
        return request instanceof ServletWebRequest servlet ? servlet.getRequest().getRequestURI() : "/";
    }
}
