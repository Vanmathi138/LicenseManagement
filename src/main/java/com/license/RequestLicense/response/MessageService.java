package com.license.RequestLicense.response;

import org.springframework.stereotype.Service;

@Service
public class MessageService {
	
	public String messageResponse(String key) {
		return key;
	}
	

}
