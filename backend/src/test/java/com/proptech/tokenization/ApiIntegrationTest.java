package com.proptech.tokenization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

	@Autowired
	MockMvc mvc;

	@Autowired
	ObjectMapper om;

	@Test
	void ping_is_protected_without_token() throws Exception {
		mvc.perform(get("/api/proptech/ping"))
				.andExpect(status().is(Matchers.anyOf(Matchers.is(401), Matchers.is(403))));
	}

	@Test
	void full_flow_register_login_and_proptech_services() throws Exception {
		String email = "test+" + UUID.randomUUID() + "@proptech.com";
		String password = "secret123";

		String registerJson = om.writeValueAsString(Map.of("email", email, "password", password));
		String token = mvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registerJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isString())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String tokenValue = om.readTree(token).get("token").asText();

		mvc.perform(get("/api/proptech/ping")
						.header("Authorization", "Bearer " + tokenValue))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"));

		String propertyCreate = om.writeValueAsString(Map.of(
				"title", "Apartamento Centro",
				"description", "Propiedad con alto potencial de renta.",
				"city", "Medellín",
				"country", "CO",
				"valuationUsd", new BigDecimal("120000.00")
		));

		String propResp = mvc.perform(post("/api/proptech/properties")
						.header("Authorization", "Bearer " + tokenValue)
						.contentType(MediaType.APPLICATION_JSON)
						.content(propertyCreate))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isString())
				.andReturn().getResponse().getContentAsString();

		UUID propertyId = UUID.fromString(om.readTree(propResp).get("id").asText());

		String offeringCreate = om.writeValueAsString(Map.of(
				"propertyId", propertyId,
				"totalTokens", new BigDecimal("1000.00"),
				"tokenPriceUsd", new BigDecimal("120.00")
		));

		String offeringResp = mvc.perform(post("/api/proptech/offerings")
						.header("Authorization", "Bearer " + tokenValue)
						.contentType(MediaType.APPLICATION_JSON)
						.content(offeringCreate))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DRAFT"))
				.andReturn().getResponse().getContentAsString();

		UUID offeringId = UUID.fromString(om.readTree(offeringResp).get("id").asText());

		mvc.perform(put("/api/proptech/offerings/{id}/status", offeringId)
						.header("Authorization", "Bearer " + tokenValue)
						.contentType(MediaType.APPLICATION_JSON)
						.content(om.writeValueAsString(Map.of("status", "OPEN"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("OPEN"));

		String investReq = om.writeValueAsString(Map.of(
				"offeringId", offeringId,
				"tokensRequested", new BigDecimal("10.00")
		));

		mvc.perform(post("/api/proptech/investments")
						.header("Authorization", "Bearer " + tokenValue)
						.contentType(MediaType.APPLICATION_JSON)
						.content(investReq))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACCEPTED"))
				.andExpect(jsonPath("$.amountUsd").value(1200.00));

		mvc.perform(get("/api/proptech/investments")
						.header("Authorization", "Bearer " + tokenValue)
						.param("offeringId", offeringId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(1))));

		// Also validate login works
		mvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(om.writeValueAsString(Map.of("email", email, "password", password))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isString());
	}
}

