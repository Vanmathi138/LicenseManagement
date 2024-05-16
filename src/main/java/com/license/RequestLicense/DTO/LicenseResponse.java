package com.license.RequestLicense.DTO;

import com.license.RequestLicense.Entity.License;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicenseResponse {
	private License license;
	private String message;

}
