package com.marketplace.metronome.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marketplace.metronome.DTO.AddQuantityRequest;
import com.marketplace.metronome.DTO.CreateRequest;
import com.marketplace.metronome.DTO.DecreaseRequest;
import com.marketplace.metronome.DTO.ReleaseRequest;
import com.marketplace.metronome.DTO.ReserveRequest;
import com.marketplace.metronome.service.ServiceInterface;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/inventory")
public class ControllerImpl implements ControllerInterface {

	    @Autowired
	    private ServiceInterface service;
	    @GetMapping
	    public String test() {
	        return "hello";
	    }

	    @GetMapping("/{productId}")
	    public ResponseEntity<?> get(@PathVariable String productId) {
	        return service.getProductStock(productId);
	    }

	    @PostMapping("/create")
	    public ResponseEntity<?> create(@RequestBody @Valid CreateRequest req) {
	        return service.createProduct(req);
	    }

	    @PostMapping("/add")
	    public ResponseEntity<?> add(@RequestBody @Valid AddQuantityRequest req) {
	        return service.addQuantity(req);
	    }

	    @PostMapping("/decrease")
	    public ResponseEntity<?> decrease(@RequestBody @Valid DecreaseRequest req) {
	        return service.decreaseQuantity(req);
	    }

	    @DeleteMapping("/{productId}")
	    public ResponseEntity<?> delete(@PathVariable String productId) {
	        return service.deleteProduct(productId);
	    }

	    @PostMapping("/reserve")
	    public ResponseEntity<?> reserve(@RequestBody @Valid ReserveRequest req) {
	        return service.reserveStock(req);
	    }

	    @PostMapping("/release")
	    public ResponseEntity<?> release(@RequestBody @Valid ReleaseRequest req) {
	        return service.releaseStock(req);
	    }
}
