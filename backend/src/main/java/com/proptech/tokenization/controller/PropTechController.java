package com.proptech.tokenization.controller;

import com.proptech.tokenization.dto.proptech.PropTechDtos;
import com.proptech.tokenization.service.PropertyService;
import com.proptech.tokenization.service.TokenizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/proptech")
@RequiredArgsConstructor
public class PropTechController {
	private final PropertyService propertyService;
	private final TokenizationService tokenizationService;

	@GetMapping("/ping")
	public ResponseEntity<?> ping() {
		return ResponseEntity.ok(java.util.Map.of("status", "ok"));
	}

	@PostMapping("/properties")
	public ResponseEntity<PropTechDtos.PropertyResponse> createProperty(@Valid @RequestBody PropTechDtos.PropertyCreateRequest req) {
		return ResponseEntity.ok(propertyService.create(req));
	}

	@GetMapping("/properties")
	public ResponseEntity<List<PropTechDtos.PropertyResponse>> listProperties() {
		return ResponseEntity.ok(propertyService.list());
	}

	@GetMapping("/properties/{id}")
	public ResponseEntity<PropTechDtos.PropertyResponse> getProperty(@PathVariable UUID id) {
		return ResponseEntity.ok(propertyService.get(id));
	}

	// Servicio 1 (tokenización): crear y administrar ofertas de tokens
	@PostMapping("/offerings")
	public ResponseEntity<PropTechDtos.OfferingResponse> createOffering(@Valid @RequestBody PropTechDtos.OfferingCreateRequest req) {
		return ResponseEntity.ok(tokenizationService.createOffering(req));
	}

	@GetMapping("/offerings")
	public ResponseEntity<List<PropTechDtos.OfferingResponse>> listOfferings(@RequestParam(required = false) UUID propertyId) {
		return ResponseEntity.ok(tokenizationService.listOfferings(propertyId));
	}

	@GetMapping("/offerings/{id}")
	public ResponseEntity<PropTechDtos.OfferingResponse> getOffering(@PathVariable UUID id) {
		return ResponseEntity.ok(tokenizationService.getOffering(id));
	}

	@PutMapping("/offerings/{id}/status")
	public ResponseEntity<PropTechDtos.OfferingResponse> setOfferingStatus(@PathVariable UUID id, @Valid @RequestBody PropTechDtos.OfferingStatusRequest req) {
		return ResponseEntity.ok(tokenizationService.setOfferingStatus(id, req.getStatus()));
	}

	// Servicio 2 (inversión): invertir en una oferta (compra de tokens)
	@PostMapping("/investments")
	public ResponseEntity<PropTechDtos.InvestmentResponse> invest(@Valid @RequestBody PropTechDtos.InvestRequest req, Authentication auth) {
		String investor = auth == null ? "unknown" : auth.getName();
		return ResponseEntity.ok(tokenizationService.invest(req.getOfferingId(), investor, req.getTokensRequested()));
	}

	@GetMapping("/investments")
	public ResponseEntity<List<PropTechDtos.InvestmentResponse>> listInvestments(@RequestParam UUID offeringId) {
		return ResponseEntity.ok(tokenizationService.listInvestments(offeringId));
	}
}

