package valleysoft.minibasket.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final EmailProperties properties;

    @Override
    public void send(EmailMessage email) {
        String html = templateEngine.process(email.template(), new Context(null, email.variables()));
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.from());
            helper.setTo(email.to());
            helper.setSubject(email.subject());
            helper.setText(email.plainText(), html);
            mailSender.send(message);
            log.info("Sent '{}' to {}", email.subject(), email.to());
        } catch (MessagingException ex) {
            throw new MailSendException("Could not build the email to " + email.to(), ex);
        }
    }
}
