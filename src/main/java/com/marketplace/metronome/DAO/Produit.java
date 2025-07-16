package com.marketplace.metronome.DAO;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @AllArgsConstructor @NoArgsConstructor @Data
public class Produit {
	@Id
	private String productId;
	private int availableQuantity;
	private int reservedQuantity;
	
}
