package com.license.RequestLicense.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class OtpService {

	private static final int OTP_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, OtpEntry> otpStorage = new HashMap<>();

    public String generateOtp() {
        int otp = random.nextInt(900000) + 100000; // Generates a number between 100000 and 999999
        return String.valueOf(otp); // Convert the number to a String and return it
    }

    public void storeOtp(String email, String otp) {
        long expirationTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutes from now
        otpStorage.put(email, new OtpEntry(otp, expirationTime));
    }

    public boolean validateOtp(String email, String otp) {
        OtpEntry otpEntry = otpStorage.get(email);
        if (otpEntry == null) {
            return false; // No OTP for this email
        }
        if (System.currentTimeMillis() > otpEntry.getExpirationTime()) {
            otpStorage.remove(email); // Remove expired OTP
            return false; // OTP is expired
        }
        return otpEntry.getOtp().equals(otp); // Check if OTP matches
    }

    private static class OtpEntry {
        private final String otp;
        private final long expirationTime;

        public OtpEntry(String otp, long expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }

        public String getOtp() {
            return otp;
        }

        public long getExpirationTime() {
            return expirationTime;
        }
    }
}
