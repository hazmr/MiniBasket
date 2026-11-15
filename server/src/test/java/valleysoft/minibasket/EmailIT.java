package valleysoft.minibasket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.icegreen.greenmail.util.GreenMail;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class EmailIT extends IntegrationTest {

    private static final Pattern TOKEN_LINK = Pattern.compile("(http\\S+)\\?token=(\\S+)");

    @Autowired private GreenMail greenMail;

    @Test
    void register_sendsVerificationEmail_whoseLinkVerifiesTheAccount() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, PASSWORD)))
                .andExpect(status().isCreated());

        MimeMessage message = awaitEmailTo(email);
        assertThat(message.getSubject()).isEqualTo("Verify your MiniBasket email");
        assertThat(htmlOf(message)).contains("Verify my email");
        Matcher link = TOKEN_LINK.matcher(plainTextOf(message));
        assertThat(link.find()).isTrue();
        assertThat(link.group(1)).isEqualTo("http://localhost:3000/verify-email");

        mockMvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content("""
                        {"token": "%s"}
                        """.formatted(URLDecoder.decode(link.group(2), StandardCharsets.UTF_8))))
                .andExpect(status().isNoContent());
        login(email);
    }

    @Test
    void forgotPassword_sendsResetEmail_whoseTokenChangesThePassword() throws Exception {
        String email = uniqueEmail();
        createVerifiedUser(email, "CUSTOMER");

        mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s"}
                        """.formatted(email)))
                .andExpect(status().isNoContent());

        MimeMessage message = awaitEmailTo(email);
        assertThat(message.getSubject()).isEqualTo("Reset your MiniBasket password");
        Matcher link = TOKEN_LINK.matcher(plainTextOf(message));
        assertThat(link.find()).isTrue();
        assertThat(link.group(1)).isEqualTo("http://localhost:3000/reset-password");

        mockMvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content("""
                        {"token": "%s", "newPassword": "brandNew789"}
                        """.formatted(URLDecoder.decode(link.group(2), StandardCharsets.UTF_8))))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s", "password": "brandNew789"}
                        """.formatted(email)))
                .andExpect(status().isOk());
    }

    @Test
    void forgotPassword_forUnknownEmail_sendsNothing() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s"}
                        """.formatted(email)))
                .andExpect(status().isNoContent());

        Thread.sleep(1000);
        assertThat(greenMail.getReceivedMessagesForDomain(email)).isEmpty();
    }

    private MimeMessage awaitEmailTo(String email) throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            MimeMessage[] messages = greenMail.getReceivedMessagesForDomain(email);
            if (messages.length > 0) {
                return messages[0];
            }
            Thread.sleep(100);
        }
        throw new AssertionError("No email arrived for " + email);
    }

    private static String plainTextOf(MimeMessage message) throws Exception {
        return partOf(message, "text/plain");
    }

    private static String htmlOf(MimeMessage message) throws Exception {
        return partOf(message, "text/html");
    }

    private static String partOf(MimeMessage message, String type) throws Exception {
        return findPart((Multipart) message.getContent(), type);
    }

    private static String findPart(Multipart multipart, String type) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            if (part.isMimeType(type)) {
                return (String) part.getContent();
            }
            if (part.getContent() instanceof Multipart nested) {
                String found = findPart(nested, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
