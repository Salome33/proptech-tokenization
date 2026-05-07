package com.proptech.tokenization.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "token_offerings")
public class TokenOffering {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private java.util.UUID id;

	@Column(nullable = false)
	private java.util.UUID propertyId;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal totalTokens;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal tokenPriceUsd;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal tokensSold;

	@Column(nullable = false)
	private String status; // DRAFT | OPEN | CLOSED

	@Column(nullable = false)
	private Instant createdAt;
}

