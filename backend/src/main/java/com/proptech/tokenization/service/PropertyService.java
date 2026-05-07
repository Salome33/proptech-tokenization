package com.proptech.tokenization.service;

import com.proptech.tokenization.dto.proptech.PropTechDtos;
import com.proptech.tokenization.model.PropertyAsset;
import com.proptech.tokenization.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyService {
	private final PropertyRepository propertyRepository;

	public PropTechDtos.PropertyResponse create(PropTechDtos.PropertyCreateRequest req) {
		PropertyAsset saved = propertyRepository.save(PropertyAsset.builder()
				.title(req.getTitle().trim())
				.description(req.getDescription().trim())
				.city(req.getCity().trim())
				.country(req.getCountry().trim())
				.valuationUsd(req.getValuationUsd())
				.createdAt(Instant.now())
				.build());

		return toResponse(saved);
	}

	public List<PropTechDtos.PropertyResponse> list() {
		return propertyRepository.findAll().stream().map(this::toResponse).toList();
	}

	public PropTechDtos.PropertyResponse get(UUID id) {
		PropertyAsset p = propertyRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada."));
		return toResponse(p);
	}

	private PropTechDtos.PropertyResponse toResponse(PropertyAsset p) {
		return PropTechDtos.PropertyResponse.builder()
				.id(p.getId())
				.title(p.getTitle())
				.description(p.getDescription())
				.city(p.getCity())
				.country(p.getCountry())
				.valuationUsd(p.getValuationUsd())
				.createdAt(p.getCreatedAt())
				.build();
	}
}

