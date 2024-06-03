package com.license.RequestLicense.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.license.RequestLicense.DTO.LicenseDto;
import com.license.RequestLicense.Repository.LicenseRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailSender {
	private final JavaMailSender mailSender;
	private final OtpService otpService;
	
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

	public void sendOtp(String to, String otp) {		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject("Your OTP Code");
		message.setText("Your OTP code is: " + otp + "\n\nThis OTP is valid for 5 minutes only.");
		mailSender.send(message);
	}
}
