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
@Table(name = "properties")
public class PropertyAsset {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private java.util.UUID id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 2000)
	private String description;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String country;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valuationUsd;

	@Column(nullable = false)
	private Instant createdAt;
}

