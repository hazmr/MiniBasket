package valleysoft.minibasket.order.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class OrderNumberGenerator {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int SUFFIX_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();
    private final Clock clock;

    public OrderNumberGenerator() {
        this(Clock.systemDefaultZone());
    }

    OrderNumberGenerator(Clock clock) {
        this.clock = clock;
    }

    public String next() {
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            suffix.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return "MB-" + LocalDate.now(clock).format(DATE) + "-" + suffix;
    }
}
