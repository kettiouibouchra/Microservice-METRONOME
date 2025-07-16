package com.marketplace.metronome.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRequest {
	@NotBlank
	private String productId;
	@Min(0)
	private int initialQuantity=0;
	
}
