package valleysoft.minibasket.common.exception;

import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import valleysoft.minibasket.common.web.TraceIdFilter;

@Component
@RequiredArgsConstructor
public class ApiProblems {

    public static final String ERRORS_METRIC = "minibasket.api.errors";

    private final MeterRegistry meterRegistry;

    public ProblemDetail create(ErrorCode code, String detail, Map<String, ?> params, String path) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(code.status(), detail);
        problem.setInstance(URI.create(path));
        if (!params.isEmpty()) {
            problem.setProperty("params", params);
        }
        return addContract(problem, code);
    }

    public ProblemDetail addContract(ProblemDetail problem, ErrorCode code) {
        problem.setProperty("errorCode", code.name());
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        if (traceId != null) {
            problem.setProperty("traceId", traceId);
        }
        meterRegistry.counter(ERRORS_METRIC, "code", code.name(), "status", String.valueOf(code.status().value()))
                .increment();
        return problem;
    }
}
