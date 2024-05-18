package com.license.RequestLicense.Service;

import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import com.license.RequestLicense.DTO.LicenseDto;
import com.license.RequestLicense.Entity.License;
import com.license.RequestLicense.Enumeration.ExpiryStatus;
import com.license.RequestLicense.Enumeration.Status;
import com.license.RequestLicense.Repository.LicenseRepository;
import com.license.RequestLicense.response.MessageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LicenseService {
	private final LicenseRepository repository;
	private final MessageService messageService;
	private final LicenseGenerator licenseGenerator;
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

// Method to decrypt data using a secret key
	private String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
		return new String(decryptedBytes);
	}

//create
	public License saveLicense(LicenseDto licenseDto) {
		License license = new License();
		license = License.builder().companyName(licenseDto.getCompanyName()).email(licenseDto.getEmail())
				.status(Status.REQUEST).build();
		return repository.save(license);
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

//encrypt license key and mail with use of secret key
	public ResponseEntity<EncryptedData> encryptEmailAndLicenseKey(String companyName) {
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



    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DecryptedData decryptEncryptedData(EncryptedData encryptedDataDto) throws Exception {
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
        	LocalDateTime today =LocalDateTime.now();
        	LocalDateTime activationDate = LocalDateTime.now(); // Activation time is now
        	LocalDateTime expiryDate = activationDate.plusMinutes(3); // Expiry time is 5 minutes from activation
 
        	//long minutesUntilExpiration = ChronoUnit.MINUTES.between(today, expiryDate);
            //String gracePeriod = minutesUntilExpiration + " mins";
            
            ExpiryStatus expiryStatus = today.isBefore(activationDate) ? ExpiryStatus.NOT_ACTIVATED : ExpiryStatus.ACTIVE;
            
            if (license.getEmail().equals(decryptedEmail) && license.getLicenseKey().equals(decryptedLicenseKey)
                    && license.getStatus().equals(Status.REQUEST)) {

                license.setStatus(Status.APPROVED);
                license.setActivationDate(activationDate);
                license.setExpiryDate(expiryDate);
                license.setExpiryStatus(expiryStatus);

                //license.setGracePeriod(gracePeriod);
                repository.save(license);

                return new DecryptedData(decryptedEmail, decryptedLicenseKey);
            } else {
                throw new IllegalArgumentException("Decryption failed");
            }
        }).orElseThrow(() -> new IllegalArgumentException(messageService.messageResponse("Invalid encrypted data")));
	}

   /* @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    public void updateGracePeriod() {
        List<License> license = repository.findAll();
        LocalDate today = LocalDate.now();

        for (License licenseGracePeriod : license) {
            LocalDate expireDate = licenseGracePeriod.getExpiryDate();
            if (expireDate != null) {
                long daysUntilExpiration = ChronoUnit.DAYS.between(today, expireDate);
                if (daysUntilExpiration >= 0) {
                    String gracePeriod = daysUntilExpiration + " days";
                    licenseGracePeriod.setGracePeriod(gracePeriod);
                    repository.save(licenseGracePeriod);
                }
            }
        }
    }*/
    @Scheduled(cron = "0 * * * * ?") // Runs every minute
    public void updateGracePeriodInMins() {
        List<License> licenses = repository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (License license : licenses) {
            LocalDateTime activation = license.getActivationDate();
            if (activation != null) {
                LocalDateTime expiry = activation.plusMinutes(3);
                
                if(now.isAfter(expiry)) {
                	LocalDateTime graceStart = expiry.plusMinutes(2);
                	license.setExpiryStatus(ExpiryStatus.EXPIRED);
                	license.setGracePeriod(graceStart);                      
            
            repository.save(license);

            }
        }
    }
}
}
