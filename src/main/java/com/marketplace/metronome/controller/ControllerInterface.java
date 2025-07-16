package com.marketplace.metronome.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.marketplace.metronome.DTO.AddQuantityRequest;
import com.marketplace.metronome.DTO.CreateRequest;
import com.marketplace.metronome.DTO.DecreaseRequest;
import com.marketplace.metronome.DTO.ReleaseRequest;
import com.marketplace.metronome.DTO.ReserveRequest;

import jakarta.validation.Valid;

public interface ControllerInterface {
	public ResponseEntity<?> get(@PathVariable String productId);
	public ResponseEntity<?> create(@RequestBody @Valid CreateRequest req);
	public ResponseEntity<?> add(@RequestBody @Valid AddQuantityRequest req);
	public ResponseEntity<?> decrease(@RequestBody @Valid DecreaseRequest req);
	public ResponseEntity<?> delete(@PathVariable String productId);
	public ResponseEntity<?> reserve(@RequestBody @Valid ReserveRequest req);
	public ResponseEntity<?> release(@RequestBody @Valid ReleaseRequest req);
	
}
