package com.proptech.tokenization.repository;

import com.proptech.tokenization.model.PropertyAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PropertyRepository extends JpaRepository<PropertyAsset, UUID> {
}

