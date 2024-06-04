package com.license.RequestLicense.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.license.RequestLicense.Entity.OTP;

@Repository
public interface OtpRepository extends JpaRepository<OTP, Integer>{

	OTP findByOtp(String otp);
}
