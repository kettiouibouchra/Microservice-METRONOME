package com.marketplace.metronome.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddQuantityRequest {
	@NotBlank
    private String productId;

    @Min(1)
    private int quantity;
}

