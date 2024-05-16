package com.license.RequestLicense.exception;

import javax.validation.ValidationException;

public class LicenseInvalidException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public LicenseInvalidException(String s) {
		super(s);
	}
}
