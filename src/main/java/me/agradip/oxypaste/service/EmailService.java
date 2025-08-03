package me.agradip.oxypaste.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.security.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${application.base-url}")
    private String baseUrl;

    @Value("${spring.mail.from}")
    private String emailFrom;

    private final VerificationTokenService verificationTokenService;

    public EmailService(VerificationTokenService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }

    public void sendVerificationEmail(User user) {
        Map<String, Object> payload = UserService.generatePayloadFromObject(user);
        String token = verificationTokenService.signPayload(payload);

        String subject = "Verify your email";
        String verificationLink = baseUrl + "/account/verify?token=" + token;

        // Format the expiry timestamp
        String expiryStr = "";
        if (payload.containsKey("expInstant")) {
            Instant expiryInstant = Instant.parse((String) payload.get("expInstant"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")
                    .withZone(ZoneId.of("UTC"));
            expiryStr = formatter.format(expiryInstant) + " UTC";
        }

        String content = """
        <html>
        <body style="font-family: Arial, sans-serif; color: #333;">
            <h2 style="color: #4CAF50;">Welcome to OxyPaste, %s!</h2>
            <p>Please verify your email by clicking the button below:</p>
            <a href="%s" style="
                display: inline-block;
                padding: 10px 20px;
                margin-top: 10px;
                background-color: #4CAF50;
                color: white;
                text-decoration: none;
                border-radius: 5px;
            ">Verify Email</a>
            <p>If the button doesn't work, copy and paste the link below into your browser:</p>
            <p><a href="%s">%s</a></p>
            <br>
            <p style="font-size: 0.9em; color: #888;">This verification link will expire on <strong>%s</strong>.</p>
        </body>
        </html>
        """.formatted(user.getUsername(), verificationLink, verificationLink, verificationLink, expiryStr);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(emailFrom);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

}
