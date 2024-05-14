package com.license.RequestLicense.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.license.RequestLicense.Enumeration.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="license")
public class License {
	 	@Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    @Column(name="id")
	    private Long id;

	    @Column(name="company_name")
	    private String companyName;

	    @Column(name="email")
	    private String email;

	    @Column(name="grace_period")
	    private String gracePeriod;

	    @Column(name = "status")
	    @Enumerated(EnumType.STRING)
	    private Status status;

	    @Column(name = "activation_date", updatable = false)
	    private LocalDateTime activationDate;

	    @Column(name="expiry_date")
	    private LocalDate expiryDate;

	    @Column(name="expiry_status")
	    private LocalDate expiryStatus;
	    
	    @Column(name="license_key")
	    private String licenseKey;
	


}
