package com.proptech.tokenization.service;

import com.proptech.tokenization.dto.proptech.PropTechDtos;
import com.proptech.tokenization.model.InvestmentOrder;
import com.proptech.tokenization.model.PropertyAsset;
import com.proptech.tokenization.model.TokenOffering;
import com.proptech.tokenization.repository.InvestmentOrderRepository;
import com.proptech.tokenization.repository.PropertyRepository;
import com.proptech.tokenization.repository.TokenOfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenizationService {
	private final PropertyRepository propertyRepository;
	private final TokenOfferingRepository offeringRepository;
	private final InvestmentOrderRepository orderRepository;

	public PropTechDtos.OfferingResponse createOffering(PropTechDtos.OfferingCreateRequest req) {
		PropertyAsset property = propertyRepository.findById(req.getPropertyId())
				.orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada."));

		BigDecimal valuation = property.getValuationUsd();
		BigDecimal implied = req.getTotalTokens().multiply(req.getTokenPriceUsd());
		BigDecimal min = valuation.multiply(new BigDecimal("0.50"));
		BigDecimal max = valuation.multiply(new BigDecimal("1.50"));
		if (implied.compareTo(min) < 0 || implied.compareTo(max) > 0) {
			throw new IllegalArgumentException("La oferta no es consistente con la valoración (debe estar entre 50% y 150%).");
		}

		TokenOffering saved = offeringRepository.save(TokenOffering.builder()
				.propertyId(req.getPropertyId())
				.totalTokens(req.getTotalTokens().setScale(2, RoundingMode.HALF_UP))
				.tokenPriceUsd(req.getTokenPriceUsd().setScale(2, RoundingMode.HALF_UP))
				.tokensSold(new BigDecimal("0.00"))
				.status("DRAFT")
				.createdAt(Instant.now())
				.build());

		return toOffering(saved);
	}

	public List<PropTechDtos.OfferingResponse> listOfferings(UUID propertyId) {
		List<TokenOffering> list = propertyId == null ? offeringRepository.findAll() : offeringRepository.findByPropertyId(propertyId);
		return list.stream().map(this::toOffering).toList();
	}

	public PropTechDtos.OfferingResponse getOffering(UUID id) {
		TokenOffering o = offeringRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Oferta no encontrada."));
		return toOffering(o);
	}

	public PropTechDtos.OfferingResponse setOfferingStatus(UUID offeringId, String statusRaw) {
		String status = statusRaw == null ? "" : statusRaw.trim().toUpperCase();
		if (!status.equals("OPEN") && !status.equals("CLOSED")) {
			throw new IllegalArgumentException("Estado inválido. Usa OPEN o CLOSED.");
		}

		TokenOffering o = offeringRepository.findById(offeringId)
				.orElseThrow(() -> new IllegalArgumentException("Oferta no encontrada."));

		if (o.getStatus().equals("CLOSED")) {
			return toOffering(o);
		}
		if (o.getStatus().equals("DRAFT") && status.equals("CLOSED")) {
			throw new IllegalArgumentException("No puedes cerrar una oferta en DRAFT. Primero ábrela (OPEN).");
		}

		o.setStatus(status);
		return toOffering(offeringRepository.save(o));
	}

	@Transactional
	public PropTechDtos.InvestmentResponse invest(UUID offeringId, String investorEmail, BigDecimal tokensRequested) {
		if (tokensRequested == null || tokensRequested.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("tokensRequested debe ser mayor a 0.");
		}

		TokenOffering offering = offeringRepository.findById(offeringId)
				.orElseThrow(() -> new IllegalArgumentException("Oferta no encontrada."));
		if (!"OPEN".equals(offering.getStatus())) {
			throw new IllegalArgumentException("La oferta no está abierta.");
		}

		BigDecimal remaining = offering.getTotalTokens().subtract(offering.getTokensSold());
		if (tokensRequested.compareTo(remaining) > 0) {
			throw new IllegalArgumentException("No hay tokens suficientes disponibles.");
		}

		BigDecimal amount = tokensRequested.multiply(offering.getTokenPriceUsd()).setScale(2, RoundingMode.HALF_UP);
		InvestmentOrder order = orderRepository.save(InvestmentOrder.builder()
				.offeringId(offering.getId())
				.investorEmail(investorEmail)
				.tokensRequested(tokensRequested.setScale(2, RoundingMode.HALF_UP))
				.amountUsd(amount)
				.status("ACCEPTED")
				.createdAt(Instant.now())
				.build());

		offering.setTokensSold(offering.getTokensSold().add(order.getTokensRequested()).setScale(2, RoundingMode.HALF_UP));
		offeringRepository.save(offering);

		return PropTechDtos.InvestmentResponse.builder()
				.id(order.getId())
				.offeringId(order.getOfferingId())
				.investorEmail(order.getInvestorEmail())
				.tokensRequested(order.getTokensRequested())
				.amountUsd(order.getAmountUsd())
				.status(order.getStatus())
				.createdAt(order.getCreatedAt())
				.build();
	}

	public List<PropTechDtos.InvestmentResponse> listInvestments(UUID offeringId) {
		return orderRepository.findByOfferingId(offeringId).stream().map(o ->
				PropTechDtos.InvestmentResponse.builder()
						.id(o.getId())
						.offeringId(o.getOfferingId())
						.investorEmail(o.getInvestorEmail())
						.tokensRequested(o.getTokensRequested())
						.amountUsd(o.getAmountUsd())
						.status(o.getStatus())
						.createdAt(o.getCreatedAt())
						.build()
		).toList();
	}

	private PropTechDtos.OfferingResponse toOffering(TokenOffering o) {
		return PropTechDtos.OfferingResponse.builder()
				.id(o.getId())
				.propertyId(o.getPropertyId())
				.totalTokens(o.getTotalTokens())
				.tokenPriceUsd(o.getTokenPriceUsd())
				.tokensSold(o.getTokensSold())
				.status(o.getStatus())
				.createdAt(o.getCreatedAt())
				.build();
	}
}

