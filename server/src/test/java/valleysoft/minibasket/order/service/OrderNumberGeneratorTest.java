package valleysoft.minibasket.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OrderNumberGeneratorTest {

    private final OrderNumberGenerator generator =
            new OrderNumberGenerator(Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void next_hasTheDateAndSixReadableCharacters() {
        assertThat(generator.next()).matches("MB-20261115-[A-HJ-NP-Z2-9]{6}");
    }

    @Test
    void next_doesNotRepeatInPractice() {
        Set<String> numbers = new HashSet<>();
        for (int i = 0; i < 1_000; i++) {
            numbers.add(generator.next());
        }
        assertThat(numbers).hasSize(1_000);
    }
}
