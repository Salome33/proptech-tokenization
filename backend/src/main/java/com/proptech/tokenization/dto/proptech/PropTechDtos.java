package com.proptech.tokenization.dto.proptech;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PropTechDtos {
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PropertyCreateRequest {
		@NotBlank
		private String title;
		@NotBlank
		private String description;
		@NotBlank
		private String city;
		@NotBlank
		private String country;
		@NotNull
		@DecimalMin("1.00")
		private BigDecimal valuationUsd;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PropertyResponse {
		private UUID id;
		private String title;
		private String description;
		private String city;
		private String country;
		private BigDecimal valuationUsd;
		private Instant createdAt;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OfferingCreateRequest {
		@NotNull
		private UUID propertyId;
		@NotNull
		@DecimalMin("1.00")
		private BigDecimal totalTokens;
		@NotNull
		@DecimalMin("0.01")
		private BigDecimal tokenPriceUsd;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OfferingResponse {
		private UUID id;
		private UUID propertyId;
		private BigDecimal totalTokens;
		private BigDecimal tokenPriceUsd;
		private BigDecimal tokensSold;
		private String status;
		private Instant createdAt;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OfferingStatusRequest {
		@NotBlank
		private String status; // OPEN | CLOSED
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class InvestRequest {
		@NotNull
		private UUID offeringId;
		@NotNull
		@DecimalMin("0.01")
		private BigDecimal tokensRequested;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class InvestmentResponse {
		private UUID id;
		private UUID offeringId;
		private String investorEmail;
		private BigDecimal tokensRequested;
		private BigDecimal amountUsd;
		private String status;
		private Instant createdAt;
	}
}

