package com.license.RequestLicense.Service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailSender {
	private final JavaMailSender mailSender;
	
	public void sendMail(String to, String subject, String secretKey, String encryptedData) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message,true);
			
			helper.setTo(to);
			helper.setSubject(subject);
			 String content = String.format(
		                "Here are the encrypted details:\n\nSecret Key: %s\n\nEncrypted Data: %s",
		                secretKey,
		                encryptedData
		            );
			helper.setText(content);
			
			mailSender.send(message);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
