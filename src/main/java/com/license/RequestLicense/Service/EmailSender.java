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
	
	public void sendMail(String to, String subject, String content) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message,true);
			
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(content);
			
			mailSender.send(message);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
