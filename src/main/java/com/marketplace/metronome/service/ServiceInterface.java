package com.marketplace.metronome.service;

import org.springframework.http.ResponseEntity;

import com.marketplace.metronome.DTO.AddQuantityRequest;
import com.marketplace.metronome.DTO.CreateRequest;
import com.marketplace.metronome.DTO.DecreaseRequest;
import com.marketplace.metronome.DTO.ReleaseRequest;
import com.marketplace.metronome.DTO.ReserveRequest;

public interface ServiceInterface {
	public ResponseEntity<?> getProductStock(String productId);
	public ResponseEntity<?> createProduct(CreateRequest request);
	public ResponseEntity<?> addQuantity(AddQuantityRequest request);
	public ResponseEntity<?> decreaseQuantity(DecreaseRequest request);
	public ResponseEntity<?> deleteProduct(String id);
	public ResponseEntity<?> reserveStock(ReserveRequest request);
	public ResponseEntity<?> releaseStock(ReleaseRequest request);

}
