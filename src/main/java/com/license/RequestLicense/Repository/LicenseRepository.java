package com.license.RequestLicense.Repository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional; 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.license.RequestLicense.Entity.License;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

	Optional<License> findByCompanyName(String companyName);

	Optional<License> findByEmailAndLicenseKey(String email, String licenseKey);
	
}
