package com.marketplace.metronome.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.marketplace.metronome.DAO.Produit;
import com.marketplace.metronome.DTO.AddQuantityRequest;
import com.marketplace.metronome.DTO.CreateRequest;
import com.marketplace.metronome.DTO.DecreaseRequest;
import com.marketplace.metronome.DTO.ReleaseRequest;
import com.marketplace.metronome.DTO.ReserveRequest;
import com.marketplace.metronome.repository.ProduitRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ServiceImpl implements ServiceInterface {
    
    private static final String QUANTITY_POSITIVE_MESSAGE = "La quantité doit être un nombre positif";
    private static final String UNAUTHORIZED_MESSAGE = "Token JWT manquant ou invalide";
    private static final String FORBIDDEN_MESSAGE = "Vous n'avez pas les permissions nécessaires";
    private static final String PRODUCT_NOT_FOUND_FORMAT = "Produit %s non trouvé";
    private static final String INSUFFICIENT_STOCK_MESSAGE = "Stock insuffisant";
    private static final String RESERVATION_ID_REQUIRED = "Le champ reservation_id est obligatoire";
    private static final String INSUFFICIENT_RESERVED_QUANTITY = "Quantité réservée insuffisante";
    
    private final String MOCK_ARIA_BASE = "http://localhost:8080/mock-aria";

    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ProduitRepository repository;

    private ResponseEntity<Map<String, Object>> badRequestResponse(String message, String path, String field) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "statusCode", HttpStatus.BAD_REQUEST.value(),
            "error", "Bad Request",
            "message", message,
            "timestamp", Instant.now().toString(),
            "path", path,
            "validationErrors", List.of(Map.of("field", field, "message", message))
        ));
    }

    private ResponseEntity<Map<String, Object>> unauthorizedResponse(String path) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
            "statusCode", HttpStatus.UNAUTHORIZED.value(),
            "error", "Unauthorized",
            "message", UNAUTHORIZED_MESSAGE,
            "timestamp", Instant.now().toString(),
            "path", path
        ));
    }

    private ResponseEntity<Map<String, Object>> forbiddenResponse(String path) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "statusCode", HttpStatus.FORBIDDEN.value(),
            "error", "Forbidden",
            "message", FORBIDDEN_MESSAGE,
            "timestamp", Instant.now().toString(),
            "path", path,
            "details", Map.of("requiredRole", "admin", "userRole", "client")
        ));
    }

    private ResponseEntity<Map<String, Object>> notFoundResponse(String productId, String path) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "statusCode", HttpStatus.NOT_FOUND.value(),
            "error", "Not Found",
            "message", String.format(PRODUCT_NOT_FOUND_FORMAT, productId),
            "timestamp", Instant.now().toString(),
            "path", path
        ));
    }

    private ResponseEntity<?> validateRequest(Integer quantity, String path) {
        if (quantity <= 0) {
            return badRequestResponse(QUANTITY_POSITIVE_MESSAGE, path, "quantity");
        }
        return null;
    }

    private ResponseEntity<?> checkAuthAndPermissions(String path) {
        if (!isUserAuthenticated()) {
            return unauthorizedResponse(path);
        }
        if (!hasAdminPermission()) {
            return forbiddenResponse(path);
        }
        return null;
    }

    @Override
    public ResponseEntity<?> getProductStock(String productId) {
        return repository.findById(productId)
                .<ResponseEntity<?>>map(product -> ResponseEntity.ok(Map.of(
                        "product_id", product.getProductId(),
                        "available_quantity", product.getAvailableQuantity(),
                        "reserved_quantity", product.getReservedQuantity()
                )))
                .orElse(notFoundResponse(productId, "/inventory/" + productId));
    }

    @Override
    public ResponseEntity<?> createProduct(CreateRequest request) {
        try {
            if (request.getInitialQuantity() < 0) {
                return badRequestResponse(QUANTITY_POSITIVE_MESSAGE, "/inventory/add", "quantity");
            }

            ResponseEntity<?> authResponse = checkAuthAndPermissions("/inventory/create");
            if (authResponse != null) {
                return authResponse;
            }

            if (repository.existsById(request.getProductId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "statusCode", HttpStatus.CONFLICT.value(),
                    "message", String.format("Produit %s déjà existant", request.getProductId())
                ));
            }

            Produit stock = new Produit(request.getProductId(), request.getInitialQuantity(), 0);
            repository.save(stock);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Produit ajouté au stock",
                "product_id", stock.getProductId(),
                "initial_quantity", request.getInitialQuantity()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal Server Error",
                "message", "Une erreur est survenue"
            ));
        }
    }

    @Override
    public ResponseEntity<?> addQuantity(AddQuantityRequest request) {
        ResponseEntity<?> validationResponse = validateRequest(request.getQuantity(), "/inventory/add");
        if (validationResponse != null) {
            return validationResponse;
        }

        ResponseEntity<?> authResponse = checkAuthAndPermissions("/inventory/add");
        if (authResponse != null) {
            return authResponse;
        }

        return repository.findById(request.getProductId())
                .<ResponseEntity<?>>map(stock -> {
                    stock.setAvailableQuantity(stock.getAvailableQuantity() + request.getQuantity());
                    repository.save(stock);
                    return ResponseEntity.ok(Map.of(
                        "message", "Quantité ajoutée avec succès",
                        "new_quantity", stock.getAvailableQuantity()
                    ));
                })
                .orElse(notFoundResponse(request.getProductId(), "/inventory/add"));
    }

    @Override
    public ResponseEntity<?> decreaseQuantity(DecreaseRequest request) {
        ResponseEntity<?> validationResponse = validateRequest(request.getQuantity(), "/inventory/decrease");
        if (validationResponse != null) {
            return validationResponse;
        }

        ResponseEntity<?> authResponse = checkAuthAndPermissions("/inventory/decrease");
        if (authResponse != null) {
            return authResponse;
        }

        return repository.findById(request.getProductId())
                .<ResponseEntity<?>>map(stock -> {
                    if (stock.getAvailableQuantity() < request.getQuantity()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                            "statusCode", HttpStatus.BAD_REQUEST.value(),
                            "error", "Bad Request",
                            "message", INSUFFICIENT_STOCK_MESSAGE,
                            "timestamp", Instant.now().toString(),
                            "path", "/inventory/decrease"
                        ));
                    }

                    stock.setAvailableQuantity(stock.getAvailableQuantity() - request.getQuantity());
                    repository.save(stock);
                    return ResponseEntity.ok(Map.of(
                        "message", "Quantité retirée avec succès",
                        "new_quantity", stock.getAvailableQuantity()
                    ));
                })
                .orElse(notFoundResponse(request.getProductId(), "/inventory/decrease"));
    }

    @Override
    public ResponseEntity<?> deleteProduct(String id) {
        ResponseEntity<?> authResponse = checkAuthAndPermissions("/inventory/" + id);
        if (authResponse != null) {
            return authResponse;
        }

        if (!repository.existsById(id)) {
            return notFoundResponse(id, "/inventory/" + id);
        }

        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Product " + id + " deleted from inventory"));
    }

    @Override
    public ResponseEntity<?> reserveStock(ReserveRequest request) {
        ResponseEntity<?> validationResponse = validateRequest(request.getQuantity(), "/inventory/reserve");
        if (validationResponse != null) {
            return validationResponse;
        }

        ResponseEntity<?> authResponse = checkAuthAndPermissions("/inventory/reserve");
        if (authResponse != null) {
            return authResponse;
        }

        return repository.findById(request.getProductId())
                .<ResponseEntity<?>>map(stock -> {
                    if (stock.getAvailableQuantity() < request.getQuantity()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                            "statusCode", HttpStatus.BAD_REQUEST.value(),
                            "error", "Bad Request",
                            "message", INSUFFICIENT_STOCK_MESSAGE,
                            "timestamp", Instant.now().toString(),
                            "path", "/inventory/reserve"
                        ));
                    }

                    stock.setAvailableQuantity(stock.getAvailableQuantity() - request.getQuantity());
                    stock.setReservedQuantity(stock.getReservedQuantity() + request.getQuantity());
                    repository.save(stock);
                    return ResponseEntity.ok(Map.of(
                        "reserved_quantity", request.getQuantity(),
                        "reservation_id", UUID.randomUUID().toString()
                    ));
                })
                .orElse(notFoundResponse(request.getProductId(), "/inventory/reserve"));
    }

    @Override
    public ResponseEntity<?> releaseStock(ReleaseRequest request) {
        if (request.getQuantity() <= 0) {
            return badRequestResponse(QUANTITY_POSITIVE_MESSAGE, "/inventory/release", "quantity");
        }

        if (request.getReservationId() == null || request.getReservationId().isBlank()) {
            return badRequestResponse(RESERVATION_ID_REQUIRED, "/inventory/release", "reservation_id");
        }

        ResponseEntity<?> authResponse = checkAuthAndPermissions("/inventory/release");
        if (authResponse != null) {
            return authResponse;
        }

        return repository.findById(request.getProductId())
                .<ResponseEntity<?>>map(stock -> {
                    if (stock.getReservedQuantity() < request.getQuantity()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                            "statusCode", HttpStatus.BAD_REQUEST.value(),
                            "error", "Bad Request",
                            "message", INSUFFICIENT_RESERVED_QUANTITY,
                            "timestamp", Instant.now().toString(),
                            "path", "/inventory/release"
                        ));
                    }

                    stock.setReservedQuantity(stock.getReservedQuantity() - request.getQuantity());
                    stock.setAvailableQuantity(stock.getAvailableQuantity() + request.getQuantity());
                    repository.save(stock);
                    return ResponseEntity.ok(Map.of(
                        "released_quantity", request.getQuantity()
                    ));
                })
                .orElse(notFoundResponse(request.getProductId(), "/inventory/release"));
    }
    
    
    private String extractTokenFromRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String token = request.getHeader("Authorization");
            System.out.println("Token extrait: " + token);
            return token;
        }
        System.out.println("Pas de requête HTTP disponible");
        return null;
    }

    private boolean isUserAuthenticated() {
        String token = extractTokenFromRequest();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                MOCK_ARIA_BASE + "/users/validate", HttpMethod.GET, request, Map.class
            );

            return Boolean.TRUE.equals(response.getBody().get("valid"));
        } catch (Exception e) {
            return false;
        }
    }
    

    private boolean hasAdminPermission() {
        String token = extractTokenFromRequest();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> validationResponse = new RestTemplate().exchange(
                MOCK_ARIA_BASE + "/users/validate", HttpMethod.GET, request, Map.class
            );

            if (!Boolean.TRUE.equals(validationResponse.getBody().get("valid"))) return false;

            String userId = (String) validationResponse.getBody().get("userId");

            ResponseEntity<Map> profileResponse = new RestTemplate().exchange(
                MOCK_ARIA_BASE + "/users/profile/" + userId, HttpMethod.GET, request, Map.class
            );

            return "admin".equals(profileResponse.getBody().get("role"));
        } catch (Exception e) {
            return false;
        }
    }
}