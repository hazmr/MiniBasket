package valleysoft.minibasket.email;

import java.util.Map;

public record EmailMessage(String to, String subject, String template, Map<String, Object> variables, String plainText) {
}
