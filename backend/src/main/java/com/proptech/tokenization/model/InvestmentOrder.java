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
@Table(name = "investment_orders")
public class InvestmentOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private java.util.UUID id;

	@Column(nullable = false)
	private java.util.UUID offeringId;

	@Column(nullable = false)
	private String investorEmail;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal tokensRequested;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amountUsd;

	@Column(nullable = false)
	private String status; // PENDING | ACCEPTED | REJECTED

	@Column(nullable = false)
	private Instant createdAt;
}

