package com.license.RequestLicense.Controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.license.RequestLicense.DTO.DecryptedData;
import com.license.RequestLicense.DTO.EncryptedData;
import com.license.RequestLicense.DTO.LicenseDto;
import com.license.RequestLicense.Entity.License;
import com.license.RequestLicense.Service.LicenseService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class LicenseController {

	private final LicenseService service;

	@PostMapping("/create")
	public ResponseEntity<License> saveLicense(@RequestBody LicenseDto licenseDto) {
		try {
			License license = service.saveLicense(licenseDto);
			return ResponseEntity.status(HttpStatus.CREATED).body(license);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PutMapping("/licenseKey/{id}")
	public ResponseEntity<License> licensegenerator(@PathVariable Long id) throws Exception {
		try {
			License license = service.licensegenerator(id);
			return ResponseEntity.ok(license);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/encryption")
	public ResponseEntity<EncryptedData> encryptEmailAndLicenseKey(
			@RequestParam String companyName,
			@RequestParam String email,
			@RequestParam String subject) {
		return service.encryptEmailAndLicenseKey(companyName,email,subject);
	}

/*	@PutMapping("/decryption")
	public ResponseEntity<DecryptedData> decryptEmailAndLicenseKey(@RequestBody EncryptedData encryptedDataDto)
			throws Exception {
		DecryptedData decryptedData = service.decryptEncryptedData(encryptedDataDto);
		if (decryptedData != null) {
			return ResponseEntity.ok(decryptedData);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}*/

	@GetMapping("/getById/{id}")
	public ResponseEntity<License> getById(@PathVariable Long id) {
		Optional<License> license = service.getById(id);
		if (license.isPresent()) {
			return ResponseEntity.ok(license.get());
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	@PutMapping("/approval")
	public ResponseEntity<License> approveLicense(@RequestBody DecryptedData decryptedData) {
		License approvedLicense = service.approval(decryptedData);
		return ResponseEntity.ok(approvedLicense);
	}
}
