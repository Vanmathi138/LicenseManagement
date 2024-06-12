package com.license.RequestLicense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.license.RequestLicense.DTO.LicenseDto;
import com.license.RequestLicense.Entity.License;
import com.license.RequestLicense.Repository.LicenseRepository;
import com.license.RequestLicense.Service.LicenseService;

public class LicenseServiceTest {
	 @Mock
	    private LicenseRepository licenseRepository;

	    @InjectMocks
	    private LicenseService licenseService;

	    @BeforeEach
	    public void setUp() {
	        MockitoAnnotations.openMocks(this);
	    }

	    @Test
	    public void testSaveLicense() {
	        License license = new License();
	        license.setId(1L);
	        license.setLicenseKey("ABC123");

	        when(licenseRepository.save(any(License.class))).thenReturn(license);

	        License savedLicense = licenseService.saveLicense(new LicenseDto());

	        assertNotNull(savedLicense);
	        assertEquals(1L, savedLicense.getId());
	        assertEquals("ABC123", savedLicense.getLicenseKey());
	    }

	    @Test
	    public void testSaveLicenseThrowsException() {
	        when(licenseRepository.save(any(License.class))).thenThrow(new RuntimeException("Database error"));

	        assertThrows(RuntimeException.class, () -> licenseService.saveLicense(new LicenseDto()));
	    }

	    @Test
	    public void testLicenseGenerator() throws Exception {
	        Long id = 1L;
	        License license = new License();
	        license.setId(id);
	        license.setLicenseKey("XYZ789");

	        when(licenseRepository.findById(anyLong())).thenReturn(Optional.of(license));
	        when(licenseRepository.save(any(License.class))).thenReturn(license);

	        License generatedLicense = licenseService.licensegenerator(id);

	        assertNotNull(generatedLicense);
	        assertEquals(id, generatedLicense.getId());
	        assertEquals("XYZ789", generatedLicense.getLicenseKey());
	    }

	    @Test
	    public void testLicenseGeneratorThrowsException() {
	        Long id = 1L;

	        when(licenseRepository.findById(anyLong())).thenReturn(Optional.empty());

	        assertThrows(Exception.class, () -> licenseService.licensegenerator(id));
	    }

}
