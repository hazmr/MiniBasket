package valleysoft.minibasket.common.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> params;

    public DomainException(ErrorCode errorCode) {
        this(errorCode, Map.of());
    }

    public DomainException(ErrorCode errorCode, Map<String, ?> params) {
        super(errorCode.render(params));
        if (!errorCode.params().equals(params.keySet())) {
            throw new IllegalArgumentException(
                    errorCode + " needs params " + errorCode.params() + " but got " + params.keySet());
        }
        this.errorCode = errorCode;
        this.params = Map.copyOf(params);
    }
}
