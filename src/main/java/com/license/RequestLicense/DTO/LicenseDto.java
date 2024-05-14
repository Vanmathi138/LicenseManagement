package com.license.RequestLicense.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LicenseDto {
	private String name;
	private String email;
	//private Status status;
	private String licenseKey;
	public String id;
	
	

}
