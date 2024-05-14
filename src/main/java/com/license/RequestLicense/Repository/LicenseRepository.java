package com.license.RequestLicense.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.license.RequestLicense.DTO.DecryptedData;
import com.license.RequestLicense.Entity.License;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

	//Optional<License> findByName(String name);

	Optional<License> findByCompanyName(String name);

}
