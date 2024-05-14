package com.license.RequestLicense.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncryptedData {
	private String secretKey;
	private String encryptedData;

}
