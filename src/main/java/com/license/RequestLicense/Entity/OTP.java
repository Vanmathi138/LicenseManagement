package com.license.RequestLicense.Entity;
import java.time.LocalTime;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "otp_1")
public class OTP {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="otp")
	private String otp;
	
	@Column(name="email")
	private String email;
	 
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalTime createdAt;
	
	
}
