package com.proptech.tokenization.repository;

import com.proptech.tokenization.model.TokenOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TokenOfferingRepository extends JpaRepository<TokenOffering, UUID> {
	List<TokenOffering> findByPropertyId(UUID propertyId);
}

