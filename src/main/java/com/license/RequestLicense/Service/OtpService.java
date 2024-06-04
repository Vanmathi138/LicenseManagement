package com.license.RequestLicense.Service;

import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.license.RequestLicense.Entity.OTP;
import com.license.RequestLicense.Repository.OtpRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {
	private final OtpRepository otpRepo;

	private static final int OTP_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();
    
    public String generateOtp() {
        int otp = random.nextInt(900000) + 100000; // Generates a number between 100000 and 999999
        return String.valueOf(otp); // Convert the number to a String and return it
    }

    public OTP storeOtp(String otp) {
    	LocalTime createAt = LocalTime.now();
    	OTP otpEntity = new OTP();
    	otpEntity.setOtp(otp);
    	otpEntity.setCreatedAt(createAt);
    	return otpRepo.save(otpEntity); 
    }
    
    public boolean validateOtp(String email, String otp) {
        OTP otpEntity = otpRepo.findByOtp(otp);
        if (otpEntity == null) {
            return false; // No OTP found
        }
        if (LocalTime.now().isAfter(otpEntity.getCreatedAt().plusMinutes(5))) {
            otpRepo.delete(otpEntity); // Remove expired OTP
            return false; // OTP is expired
        }
        return otpEntity.getOtp().equals(otp); // Check if OTP matches
    }
}
