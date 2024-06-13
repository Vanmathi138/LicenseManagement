package com.license.RequestLicense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.license.RequestLicense.Controller.LicenseController;
import com.license.RequestLicense.DTO.EncryptedData;
import com.license.RequestLicense.DTO.LicenseDto;
import com.license.RequestLicense.Entity.License;
import com.license.RequestLicense.Service.LicenseService;

public class LicenseControllerTest {
	 @InjectMocks
	    private LicenseController licenseController;

	    @Mock
	    private LicenseService licenseService;

	    @BeforeEach
	    public void setUp() {
	        MockitoAnnotations.openMocks(this);
	    }

	    @Test
	    public void testSaveLicense() {
	        LicenseDto licenseDto = new LicenseDto(); // Initialize with necessary values
	        License license = new License(); // Initialize with necessary values

	        when(licenseService.saveLicense(any(LicenseDto.class))).thenReturn(license);

	        ResponseEntity<License> responseEntity = licenseController.saveLicense(licenseDto);

	        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
	        assertEquals(license, responseEntity.getBody());
	    }

	    @Test
	    public void testSaveLicenseException() {
	        LicenseDto licenseDto = new LicenseDto(); // Initialize with necessary values

	        when(licenseService.saveLicense(any(LicenseDto.class))).thenThrow(new RuntimeException("Exception occurred"));

	        ResponseEntity<License> responseEntity = licenseController.saveLicense(licenseDto);

	        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	        assertEquals(null, responseEntity.getBody());
	    }

	    @Test
	    public void testLicenseGenerator() throws Exception {
	        Long id = 1L;
	        License license = new License(); // Initialize with necessary values

	        when(licenseService.licensegenerator(anyLong())).thenReturn(license);

	        ResponseEntity<License> responseEntity = licenseController.licensegenerator(id);

	        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	        assertEquals(license, responseEntity.getBody());
	    }

	    @Test
	    public void testLicenseGeneratorException() throws Exception {
	        Long id = 1L;

	        when(licenseService.licensegenerator(anyLong())).thenThrow(new RuntimeException("Exception occurred"));

	        ResponseEntity<License> responseEntity = licenseController.licensegenerator(id);

	        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	        assertEquals(null, responseEntity.getBody());
	    }
	    
	    //
	    @Test
	    public void testEncryptEmailAndLicenseKey() {
	        EncryptedData encryptedData = new EncryptedData();
	        encryptedData.setEncryptedData("encryptedData");
	        encryptedData.setSecretKey("secretKey");

	        when(licenseService.encryptEmailAndLicenseKey(anyString(), anyString(), anyString()))
	                .thenReturn(ResponseEntity.ok(encryptedData));

	        ResponseEntity<EncryptedData> responseEntity = licenseController.encryptEmailAndLicenseKey("companyName", "email", "subject");

	        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	        assertEquals(encryptedData, responseEntity.getBody());
	    }

	    @Test
	    public void testEncryptEmailAndLicenseKey_Exception() {
	        when(licenseService.encryptEmailAndLicenseKey(anyString(), anyString(), anyString()))
	                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));

	        ResponseEntity<EncryptedData> responseEntity = licenseController.encryptEmailAndLicenseKey("companyName", "email", "subject");

	        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	        assertEquals(null, responseEntity.getBody());
	    }

}
