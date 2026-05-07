package com.proptech.tokenization.repository;

import com.proptech.tokenization.model.InvestmentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvestmentOrderRepository extends JpaRepository<InvestmentOrder, UUID> {
	List<InvestmentOrder> findByOfferingId(UUID offeringId);
}

