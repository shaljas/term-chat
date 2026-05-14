package termchat.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/*
    The email most likely ends up in spam!
*/
public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    // Left the mail and password exposed on purpose. It is a dummy email made for this task
    private static final String FROM_EMAIL = "X";
    private static final String APP_PASSWORD = "X";

    public static void sendDMNotification(String toEmail, String senderUsername) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("New private message in term-chat");
            message.setText("You have a new private message from " + senderUsername + ". Log in to read it.");
            Transport.send(message);
        } catch (MessagingException e) {
            System.out.println("Email sending failed: " + e.getMessage());
        }
    }
}