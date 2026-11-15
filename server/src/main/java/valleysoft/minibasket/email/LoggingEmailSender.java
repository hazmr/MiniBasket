package valleysoft.minibasket.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(EmailMessage email) {
        log.info("[EMAIL] Email is off. '{}' for {}: {}", email.subject(), email.to(), email.plainText());
    }
}
