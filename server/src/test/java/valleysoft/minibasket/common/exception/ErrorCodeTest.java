package valleysoft.minibasket.common.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorCodeTest {

    @Test
    void placeholders_inTheTemplate_areTheCodesParams() {
        assertThat(ErrorCode.INSUFFICIENT_STOCK.params()).isEqualTo(Set.of("available", "productName"));
        assertThat(ErrorCode.CART_EMPTY.params()).isEmpty();
    }

    @Test
    void render_fillsInTheParams() {
        String message = ErrorCode.INSUFFICIENT_STOCK.render(Map.of("available", 2, "productName", "Nova X1"));

        assertThat(message).isEqualTo("Only 2 of “Nova X1” left in stock");
    }

    @Test
    void domainException_carriesCodeParamsAndEnglishMessage() {
        var ex = new DomainException(ErrorCode.COUPON_EXPIRED, Map.of("code", "WELCOME10"));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.COUPON_EXPIRED);
        assertThat(ex.getParams()).containsEntry("code", "WELCOME10");
        assertThat(ex.getMessage()).isEqualTo("Coupon WELCOME10 has expired");
    }

    @Test
    void domainException_withMissingOrMisnamedParams_failsImmediately() {
        assertThatThrownBy(() -> new DomainException(ErrorCode.INSUFFICIENT_STOCK, Map.of("available", 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productName");
        assertThatThrownBy(() -> new DomainException(ErrorCode.CART_EMPTY, Map.of("extra", 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void everyCode_hasATemplateAndAnErrorStatus() {
        for (ErrorCode code : ErrorCode.values()) {
            assertThat(code.template()).as(code.name()).isNotBlank();
            assertThat(code.status().isError()).as(code.name()).isTrue();
        }
    }

    @Test
    void springsOwnErrors_mapToAGenericCodeByStatus() {
        assertThat(ErrorCode.forHttpStatus(404)).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(ErrorCode.forHttpStatus(405)).isEqualTo(ErrorCode.METHOD_NOT_ALLOWED);
        assertThat(ErrorCode.forHttpStatus(HttpStatus.I_AM_A_TEAPOT.value())).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(ErrorCode.forHttpStatus(503)).isEqualTo(ErrorCode.INTERNAL_ERROR);
    }
}
