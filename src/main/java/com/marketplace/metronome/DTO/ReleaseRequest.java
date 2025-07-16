package com.marketplace.metronome.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReleaseRequest {
	@NotBlank
    private String productId;

    @Min(1)
    private int quantity;

    @NotBlank
    private String reservationId;
}
