package com.groudina.ten.demo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

@Component
public class EmailServiceImpl implements IEmailService {

    @Value("${spring.mail.username}")
    private String techTeamEmail;

    @Value("${spring.mail.password}")
    private String techTeamEmailPassword;

    private final String host = "smtp.gmail.com";

    private final String port = "587";

    private Properties properties;

    public EmailServiceImpl() {
        properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.ssl.trust", host);
    }

    @PostConstruct
    public void test() {
        System.out.println("teach team email " + techTeamEmail);
        System.out.println("teach team password " + techTeamEmailPassword);

    }

    @Override
    public Mono<Void> sendEmail(String email, String link) {
        return Mono.defer(() -> {
            Session session = Session.getInstance(this.properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(techTeamEmail, techTeamEmailPassword);
                }
            });
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(techTeamEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("Account Verification");

                String msg = String.format(template, email, link);
                System.out.println(msg);

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);

                message.setContent(multipart);

                Transport.send(message);
            }
            catch (MessagingException e) {
                e.printStackTrace();
                return Mono.error(e);
            }
            return Mono.empty();
        });
    }

    private static final String template = "<div><p>Hello, %s!</p>" +
            "<p>Confirm your account registration by clicking following link: " +
            "<a href=\"%s\">link</a></p>" +
            "<p>Sincerely yours,<br>" +
            "Development Team</p></div>";
}
