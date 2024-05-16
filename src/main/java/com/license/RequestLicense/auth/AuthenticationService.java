package com.license.RequestLicense.auth;

import org.springframework.stereotype.Service;

import com.license.RequestLicense.DTO.LicenseResponse;
import com.license.RequestLicense.Repository.LicenseRepository;
import com.license.RequestLicense.Service.LicenseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final LicenseRepository repository;

	public String authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getName()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Incorrect username or password");
        }
        var user = repository.findByUserName(request.getName()).orElseThrow();
        return jwtService.generateToken(user);
    }


}
