package com.marketplace.metronome.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock-aria")
public class FakeAriaController {

    @GetMapping("/users/validate")
    public Map<String, Object> validateToken(@RequestHeader("Authorization") String token) {
        if (token.equals("Bearer valid-admin-token")) {
            return Map.of(
                "valid", true,
                "userId", "admin-id"
            );
        } else if (token.equals("Bearer valid-client-token")) {
            return Map.of(
                "valid", true,
                "userId", "client-id"
            );
        } else {
            return Map.of("valid", false);
        }
    }

    @GetMapping("/users/profile/{userId}")
    public Map<String, Object> getProfile(@PathVariable String userId) {
        if (userId.equals("admin-id")) {
            return Map.of("role", "admin");
        } else {
            return Map.of("role", "client");
        }
    }
}

