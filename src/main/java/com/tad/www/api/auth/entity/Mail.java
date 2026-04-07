package com.tad.www.api.auth.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.NonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "tb_email_auth", schema = "auth")
public class Mail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "request_id", nullable = false, unique = true)
	private UUID requestId;

	@Column(name = "email", nullable = false, columnDefinition = "citext")
	private String email;

	@Column(name = "purpose", length = 30, nullable = false)
	private String purpose;

	@Column(name = "email_verified", nullable = false)
	private boolean emailVerified = false;

	@Column(name = "verified_at")
	private OffsetDateTime verifiedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	static public Mail create(UUID requestId, String email, String purpose){
		return Mail.builder()
				.requestId(requestId)
				.email(email)
				.purpose(purpose)
				.build();
	}

}
