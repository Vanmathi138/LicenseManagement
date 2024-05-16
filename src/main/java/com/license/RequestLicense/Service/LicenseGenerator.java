package com.license.RequestLicense.Service;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import com.license.RequestLicense.Entity.License;

@Service
public class LicenseGenerator {
	private static final String ALGORITHM = "AES";
	private SecretKey secretKey = generateSecretKey();

	private SecretKey generateSecretKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(256); 
			return keyGenerator.generateKey();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
//generate License key
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

	// Method to encrypt data using a secret key
	private String encrypt(String data, Key secretKey) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedBytes = cipher.doFinal(data.getBytes());
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	// Method to decrypt data using a secret key
	private String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
		return new String(decryptedBytes);
	}

}
