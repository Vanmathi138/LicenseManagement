package com.license.RequestLicense.Service;
import java.security.Key ;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.license.RequestLicense.DTO.DecryptedData;
import com.license.RequestLicense.DTO.EncryptedData;
import com.license.RequestLicense.DTO.LicenseDto;
import com.license.RequestLicense.Entity.License;
import com.license.RequestLicense.Enumeration.Status;
import com.license.RequestLicense.Repository.LicenseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LicenseService {
	private final LicenseRepository repository;
	
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
		License license = License.builder()
				.companyName(licenseDto.getCompanyName())
	            .email(licenseDto.getEmail())
	            .status(Status.REQUEST)
	            .build();
		return repository.save(license);
	}
//generate license key
	public License licensegenerator(Long id) {
		//SecretKey secretKeys = generateSecretKey();
		 if (id == null) {
	            throw new IllegalArgumentException("Id cannot be null");
	        }
		 License license = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("License not found with id: " + id));;
		 String generatedLicense = LicenseGenerator.generateLicenseKey(license, secretKey);
		 license.setLicenseKey(generatedLicense);
		 return repository.save(license);
	}
//encrypt license key and mail with use of secret key
	public ResponseEntity<EncryptedData> encryptEmailAndLicenseKey(String name) {
	    try {
	        Optional<License> optionalLicense = repository.findByCompanyName(name);
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
	public DecryptedData decryptEncryptedData(EncryptedData encryptedDataDto) {
        String secretKeyStr = encryptedDataDto.getSecretKey();
        String encryptedData = encryptedDataDto.getEncryptedData();
        License license = new License();
        try {
            byte[] decodedKey = Base64.getDecoder().decode(secretKeyStr);
            SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            String[] parts = encryptedData.split("\\|");
            String encryptedEmail = parts[0];
            String encryptedLicenseKey = parts[1];

            String decryptedEmail = decrypt(encryptedEmail, originalKey);
            String decryptedLicenseKey = decrypt(encryptedLicenseKey, originalKey);
         
            return new DecryptedData(decryptedEmail, decryptedLicenseKey);
        } catch (Exception e) {
            e.printStackTrace();
            // You may want to handle exceptions differently, like throwing a custom exception or returning null
            return null;
        }
    }

	private static final String Algorithm="AES";
	public static String decryptLicenseKey(String encryptedLicenseKey, SecretKey secretKey) {
	    try {
	        Cipher cipher = Cipher.getInstance(Algorithm);
	        cipher.init(Cipher.DECRYPT_MODE, secretKey);
	        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedLicenseKey);
	        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
	        return new String(decryptedBytes);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
}

