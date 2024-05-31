package com.license.RequestLicense.Service;

import java.security.Key;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.license.RequestLicense.DTO.DecryptedData;
import com.license.RequestLicense.DTO.EncryptedData;
import com.license.RequestLicense.Entity.License;
import com.license.RequestLicense.Enumeration.ExpiryStatus;
import com.license.RequestLicense.Enumeration.Status;

import ch.qos.logback.classic.Logger;

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

/*	public static String decrypt(String encryptedData, String base64SecretKey) throws Exception {
		byte[] decodedKey = Base64.getDecoder().decode(base64SecretKey);
		SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decodedData = Base64.getDecoder().decode(encryptedData);
		byte[] decryptedBytes = cipher.doFinal(decodedData);

		return new String(decryptedBytes);
	}*/

	// Method to decrypt data using a secret key
	public static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
		return new String(decryptedBytes);
	}

	public ResponseEntity<EncryptedData> encryptEmailAndLicenseKey(String companyName) {
		try {
			License license = new License();
			// Encrypt the email and license key
			String encryptedEmail = encrypt(license.getEmail(), secretKey);
			String encryptedLicenseKey = encrypt(license.getLicenseKey(), secretKey);
			String encryptedLicenseKeyAndEmail = encryptedEmail + "|" + encryptedLicenseKey;

			// Create a response object to hold encrypted data and secret key
			EncryptedData responseData = new EncryptedData();
			responseData.setEncryptedData(encryptedLicenseKeyAndEmail);
			responseData.setSecretKey(Base64.getEncoder().encodeToString(secretKey.getEncoded()));

			return ResponseEntity.ok(responseData);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	public DecryptedData decryptEncryptedData(EncryptedData encryptedDataDto) throws Exception {
		String secretKeyStr = encryptedDataDto.getSecretKey();
		String encryptedData = encryptedDataDto.getEncryptedData();
		byte[] decodedKey = Base64.getDecoder().decode(secretKeyStr);
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		String[] parts = encryptedData.split("\\|");
		String encryptedEmail = parts[0];
		String encryptedLicenseKey = parts[1];

		String decryptedEmail = decrypt(encryptedEmail, originalKey);
		String decryptedLicenseKey = decrypt(encryptedLicenseKey, originalKey);

		/*
		 * if (gracePeriod.equals(expiryDate)) { throw new LicenseInvalidException(
		 * messageService.
		 * messageResponse("Your License is expired! Please renew within 1 business week"
		 * )); }
		 */

		return new DecryptedData(decryptedEmail, decryptedLicenseKey);
	}



/*	public DecryptedData decryptEncryptedData(EncryptedData encryptedDataDto) throws Exception {
		// Decrypt the secret key and encrypted data
		String secretKeyStr = encryptedDataDto.getSecretKey();
		String encryptedData = encryptedDataDto.getEncryptedData();
		byte[] decodedKey = Base64.getDecoder().decode(secretKeyStr);
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		String[] parts = encryptedData.split("\\|");
		String encryptedEmail = parts[0];
		String encryptedLicenseKey = parts[1];

		String decryptedEmail = decrypt(encryptedEmail, originalKey);
		String decryptedLicenseKey = decrypt(encryptedLicenseKey, originalKey);

		// Retrieve license information from the repository
		Optional<License> originalLicense = repository.findByEmailAndLicenseKey(decryptedEmail, decryptedLicenseKey);
		return originalLicense.map(license -> {
			LocalDateTime today = LocalDateTime.now();
			LocalDateTime activationDate = LocalDateTime.now(); // Activation time is now
			LocalDateTime expiryDate = activationDate.plusMinutes(2); // Expiry time is 5 minutes from activation

			// long minutesUntilExpiration = ChronoUnit.MINUTES.between(today, expiryDate);
			// String gracePeriod = minutesUntilExpiration + " mins";

			ExpiryStatus expiryStatus = today.isBefore(activationDate) ? ExpiryStatus.NOT_ACTIVATED
					: today.isAfter(expiryDate) ? ExpiryStatus.EXPIRED : ExpiryStatus.ACTIVE;

			if (license.getEmail().equals(decryptedEmail) && license.getLicenseKey().equals(decryptedLicenseKey)
					&& license.getStatus().equals(Status.REQUEST)) {

				license.setStatus(Status.APPROVED);
				license.setActivationDate(activationDate);
				license.setExpiryDate(expiryDate);
				license.setExpiryStatus(expiryStatus);

				LocalDateTime graceEnd = expiryDate.plusMinutes(1);
				String gracePeriod = "Grace period ends at: "
						+ graceEnd.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
				;
				license.setGracePeriod(gracePeriod);
				repository.save(license);

				return new DecryptedData(decryptedEmail, decryptedLicenseKey);
			} else {
				throw new IllegalArgumentException("Decryption failed");
			}
		}).orElseThrow(() -> new IllegalArgumentException(messageService.messageResponse("Invalid encrypted data")));
	}
*/
	
	public DecryptedData decryptEncrypted(EncryptedData encryptedDataDto) throws Exception {
		// Decrypt the secret key and encrypted data
		String secretKeyStr = encryptedDataDto.getSecretKey();
		String encryptedData = encryptedDataDto.getEncryptedData();
		byte[] decodedKey = Base64.getDecoder().decode(secretKeyStr);
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		String[] parts = encryptedData.split("\\|");
		String encryptedEmail = parts[0];
		String encryptedLicenseKey = parts[1];

		String decryptedEmail = decrypt(encryptedEmail, originalKey);
		String decryptedLicenseKey = decrypt(encryptedLicenseKey, originalKey);

		// Retrieve license information from the repository
		Optional<License> originalLicense = repository.findByEmailAndLicenseKey(decryptedEmail, decryptedLicenseKey);
		return originalLicense.map(license -> {
			LocalDateTime today = LocalDateTime.now();
			LocalDateTime activationDate = LocalDateTime.now(); // Activation time is now
			LocalDateTime expiryDate = activationDate.plusMinutes(2); // Expiry time is 5 minutes from activation

			// long minutesUntilExpiration = ChronoUnit.MINUTES.between(today, expiryDate);
			// String gracePeriod = minutesUntilExpiration + " mins";

			ExpiryStatus expiryStatus = today.isBefore(activationDate) ? ExpiryStatus.NOT_ACTIVATED
					: today.isAfter(expiryDate) ? ExpiryStatus.EXPIRED : ExpiryStatus.ACTIVE;

			if (license.getEmail().equals(decryptedEmail) && license.getLicenseKey().equals(decryptedLicenseKey)
					&& license.getStatus().equals(Status.REQUEST)) {

				license.setStatus(Status.APPROVED);
				license.setActivationDate(activationDate);
				license.setExpiryDate(expiryDate);
				license.setExpiryStatus(expiryStatus);

				LocalDateTime graceEnd = expiryDate.plusMinutes(1);
				String gracePeriod = "Grace period ends at: "
						+ graceEnd.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
				;
				license.setGracePeriod(gracePeriod);
				repository.save(license);

				return new DecryptedData(decryptedEmail, decryptedLicenseKey);
			} else {
				throw new IllegalArgumentException("Decryption failed");
			}
		}).orElseThrow(() -> new IllegalArgumentException(messageService.messageResponse("Invalid encrypted data")));
	}
*/
	/*
	 * @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight public void
	 * updateGracePeriod() { List<License> license = repository.findAll(); LocalDate
	 * today = LocalDate.now();
	 * 
	 * for (License licenseGracePeriod : license) { LocalDate expireDate =
	 * licenseGracePeriod.getExpiryDate(); if (expireDate != null) { long
	 * daysUntilExpiration = ChronoUnit.DAYS.between(today, expireDate); if
	 * (daysUntilExpiration >= 0) { String gracePeriod = daysUntilExpiration +
	 * " days"; licenseGracePeriod.setGracePeriod(gracePeriod);
	 * repository.save(licenseGracePeriod); } } } }
	 */
	//@Scheduled(cron = "*/1 * * * * ?") // Runs every minute
/*	public void updateGracePeriodInMins() {
		List<License> licenses = repository.findAll();
		LocalDateTime now = LocalDateTime.now();

		for (License license : licenses) {
			LocalDateTime activation = license.getActivationDate();
			if (activation != null) {
				LocalDateTime expiry = activation.plusMinutes(2);
				LocalDateTime graceEnd = expiry.plusMinutes(1);

				if (now.isAfter(expiry)) {
					if (now.isAfter(graceEnd)) {
						license.setGracePeriod("grace period completed");
					} else {
						// String gracePeriod = "Grace period ends at: "
						// + graceEnd.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
						// license.setGracePeriod(gracePeriod);
						Duration duration = Duration.between(now, graceEnd);
						long minutesLeft = duration.toMinutes();
						long secondsLeft = duration.minusMinutes(minutesLeft).getSeconds();
						String gracePeriod = String.format("Grace period ends in: %02d:%02d", minutesLeft, secondsLeft);
						license.setGracePeriod(gracePeriod);

					}
					license.setExpiryStatus(ExpiryStatus.EXPIRED);
					repository.save(license);
				}
			}
		}
	}

}