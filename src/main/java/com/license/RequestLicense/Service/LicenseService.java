package com.license.RequestLicense.Service;

import java.security.Key ;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.license.RequestLicense.DTO.DecryptedData;
import com.license.RequestLicense.DTO.EncryptedData;
import com.license.RequestLicense.DTO.LicenseDto;
import com.license.RequestLicense.Entity.License;
import com.license.RequestLicense.Entity.OTP;
import com.license.RequestLicense.Enumeration.ExpiryStatus;
import com.license.RequestLicense.Enumeration.Status;
import com.license.RequestLicense.Repository.LicenseRepository;
import com.license.RequestLicense.Repository.OtpRepository;
import com.license.RequestLicense.response.MessageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LicenseService {
	private final LicenseRepository repository;
	private final MessageService messageService;
	private final LicenseGenerator licenseGenerator;
	private final EmailSender emailService;
	private final OtpService otpService;
	
	private SecretKey secretKey = generateSecretKey();

	private SecretKey generateSecretKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(256); // You can adjust the key size as needed (e.g., 128, 192, or 256 bits)
			return keyGenerator.generateKey();
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
	 private final OtpRepository otpRepository;
//create
	public License saveLicense(LicenseDto licenseDto) {
		License license = new License();
		license = License.builder().companyName(licenseDto.getCompanyName()).email(licenseDto.getEmail())
				.password(licenseDto.getPassword()).status(Status.REQUEST).build();
		

        License savedLicense = repository.save(license);

        // Generate OTP and save to database
        String otp = otpService.generateOtp();
        OTP otpEntity = new OTP(0, otp, licenseDto.getEmail(),LocalTime.now());
        otpRepository.save(otpEntity); //changes

        // Send OTP via email
        emailService.sendOtp(licenseDto.getEmail(), otp);

        return savedLicense;
	}

//generate license key
	public License licensegenerator(Long id) {
		// SecretKey secretKeys = generateSecretKey();
		if (id == null) {
			throw new IllegalArgumentException("Id cannot be null");
		}
		License license = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("License not found with id: " + id));
		String generatedLicense = LicenseGenerator.generateLicenseKey(license, secretKey);
		license.setLicenseKey(generatedLicense);
		return repository.save(license);
	}

//encrypt and decrypt the license key and mail with use of secret key
	public ResponseEntity<EncryptedData> encryptEmailAndLicenseKey(String companyName, String email, String subject) {
		try {
			Optional<License> optionalLicense = repository.findByCompanyName(companyName);
			if (optionalLicense.isEmpty()) {
				// If the name doesn't exist, return a bad request response
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
			}

			License license = optionalLicense.get();
	
			// Encrypt the email and license key
			String encryptedEmail = encrypt(license.getEmail(), secretKey);
			String encryptedLicenseKey = encrypt(license.getLicenseKey(), secretKey);
			String encryptedLicenseKeyAndEmail = encryptedEmail + "|" + encryptedLicenseKey;

			// Create a response object to hold encrypted data and secret key
			EncryptedData responseData = new EncryptedData();
			responseData.setEncryptedData(encryptedLicenseKeyAndEmail);
			responseData.setSecretKey(Base64.getEncoder().encodeToString(secretKey.getEncoded()));

			// Decrypt the data
            String decryptedEmail = LicenseGenerator.decrypt(encryptedEmail, secretKey);
            String decryptedLicenseKey = LicenseGenerator.decrypt(encryptedLicenseKey, secretKey);
            String decryptedData = "Email: " + decryptedEmail + "\nLicense Key: " + decryptedLicenseKey;

			// Send the encrypted data and secret key to the admin via email
			String content= "Encrypted Data: " + responseData + "\n\nDecrypted Data:\n" + decryptedData;
			emailService.sendMail(email, subject, content);

			return ResponseEntity.ok(responseData);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	public Optional<License> getById(Long id) {

		return repository.findById(id);
	}

//	@Scheduled(cron = "*/1 * * * * ?") // Runs every minute
	/*public void updateGracePeriodInMins() {
		List<License> licenses = repository.findAll();
		LocalDateTime now = LocalDateTime.now();

		for (License license : licenses) {
			LocalDateTime activation = license.getActivationDate();
			LocalDateTime expiry = license.getExpiryDate();

			if (activation != null && expiry != null) {
				LocalDateTime graceEnd = expiry.plusMinutes(1);

				if (now.isAfter(expiry)) {
					if (now.isAfter(graceEnd)) {
						license.setGracePeriod("grace period completed");
					} else {
						Duration duration = Duration.between(now, graceEnd);
						long minutesLeft = duration.toMinutes();
						long secondsLeft = duration.minusMinutes(minutesLeft).getSeconds();
						String gracePeriod = String.format("Grace period ends in: %02d:%02d", minutesLeft, secondsLeft);
						license.setGracePeriod(gracePeriod);
					}
					license.setExpiryStatus(ExpiryStatus.EXPIRED);
					repository.save(license);
				} else if (now.isBefore(expiry) && license.getExpiryStatus() != ExpiryStatus.ACTIVE) {
					license.setExpiryStatus(ExpiryStatus.ACTIVE);
					repository.save(license);
				}
			}
		}
	}


*/
	@Scheduled(cron = "0 0 0 * * ?") // Runs once a day at midnight
	public void updateGracePeriodInMins() {
	    List<License> licenses = repository.findAll();
	    LocalDate now = LocalDate.now();

	    for (License license : licenses) {
	        LocalDate activation = license.getActivationDate();
	        LocalDate expiry = license.getExpiryDate();

	        if (activation != null && expiry != null) {
	            LocalDate graceEnd = expiry.plusDays(1);

	            if (now.isAfter(expiry)) {
	                if (now.isAfter(graceEnd)) {
	                    license.setGracePeriod("grace period completed");
	                } else {
	                    long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(now, graceEnd);
	                    String gracePeriod = String.format("Grace period ends in: %d day(s)", daysLeft);
	                    license.setGracePeriod(gracePeriod);
	                }
	                license.setExpiryStatus(ExpiryStatus.EXPIRED);
	            } else if (now.isBefore(expiry) && license.getExpiryStatus() != ExpiryStatus.ACTIVE) {
	                license.setExpiryStatus(ExpiryStatus.ACTIVE);
	            }
	            repository.save(license);
	        }
	    }
	}
	public License approval(DecryptedData decryptedData) {
	    Optional<License> originalLicenseOpt = repository.findByEmailAndLicenseKey(decryptedData.getEmail(), decryptedData.getLicenseKey());

	    if (originalLicenseOpt.isPresent()) {
	        License originalLicense = originalLicenseOpt.get();

	        if (originalLicense.getStatus().equals(Status.REQUEST)) {
	        	LocalDate today = LocalDate.now();
	            LocalDate activationDate = LocalDate.now();
	            LocalDate expiryDate = activationDate.plusDays(1);
	            //ExpiryStatus expiryStatus = ExpiryStatus.ACTIVE;
	            ExpiryStatus expiryStatus = today.isBefore(activationDate) ? ExpiryStatus.NOT_ACTIVATED
						: today.isAfter(expiryDate) ? ExpiryStatus.EXPIRED : ExpiryStatus.ACTIVE;
           

	            originalLicense.setStatus(Status.APPROVED);
	            originalLicense.setActivationDate(activationDate);
	            originalLicense.setExpiryDate(expiryDate);
	            originalLicense.setExpiryStatus(expiryStatus);

	            LocalDate graceEnd = expiryDate.plusDays(1);
	            String gracePeriod = "Grace period ends on: " + graceEnd.toString();
	            originalLicense.setGracePeriod(gracePeriod);

	            repository.save(originalLicense);

	            return originalLicense;
	        } else {
	            throw new IllegalStateException("License status is not REQUEST. Approval cannot be performed.");
	        }
	    } else {
	        throw new IllegalArgumentException("License not found for the provided email and license key.");
	    }
	}

	public ResponseEntity<EncryptedData> encryption(String companyName) {
		try {
			Optional<License> optionalLicense = repository.findByCompanyName(companyName);
			if (optionalLicense.isEmpty()) {
				// If the name doesn't exist, return a bad request response
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
			}

			License license = optionalLicense.get();

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

	public ResponseEntity<?> validateOtpAndEmail(String email, String otp) {
		
		boolean isValid = otpService.validateOtp(email, otp);
		if (isValid) {
			return ResponseEntity.ok("Email is verified!");
		} else {
			return ResponseEntity.status(400).body("Invalid or expired OTP.");
		}
	}

	public ResponseEntity<?> forgetPassword(String email, String comapanyName) {
		String otp = otpService.generateOtp();
		OTP otpEntity = otpService.storeOtp(otp);
		emailService.sendOtp(email, otp);
		return ResponseEntity.ok("sent");
	}

	public ResponseEntity<?> resetPassword(String email, String comapanyName, String otp, String password) {
		boolean isValid = otpService.validateOtp(email, otp);

		if (isValid) {
			Optional<License> optionalLicense = repository.findByCompanyName(comapanyName);

			if (optionalLicense.isEmpty()) {
				return ResponseEntity.status(400).body("Invalid Company name or email.");
			}

			License license = optionalLicense.get();
			license.setPassword(password); // Assuming License has a password field

			repository.save(license);

			return ResponseEntity.ok("Password updated successfully.");
		} else {
			return ResponseEntity.status(400).body("Invalid or expired OTP.");
		}
	}
}