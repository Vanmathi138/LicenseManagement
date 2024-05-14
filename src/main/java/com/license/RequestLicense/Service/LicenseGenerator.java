package com.license.RequestLicense.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import com.license.RequestLicense.Entity.License;

@Service
public class LicenseGenerator {
	private static final String ALGORITHM = "AES";
	public static String generateLicenseKey(License license, SecretKey secretKey) {
	        String data = license.getId() + "#" + license.getCompanyName() + "#" + license.getEmail();
	        try {
	            Cipher cipher = Cipher.getInstance(ALGORITHM);
	            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
	            return Base64.getEncoder().encodeToString(encryptedBytes);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
    }
	/*public static String generateLicense(License license) {
        String data = license.getId() + "#" + license.getName() + "#" + license.getEmail();
        String licenseKey = "";

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes());
            
            // Convert byte array to hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            licenseKey = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return licenseKey;
    }*/
   
}
