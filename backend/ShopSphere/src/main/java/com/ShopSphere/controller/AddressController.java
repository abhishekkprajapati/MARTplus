package com.ShopSphere.controller;

import com.ShopSphere.dto.AddressResponse;
import com.ShopSphere.entity.Address;
import com.ShopSphere.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<?> getAddresses(
            Authentication authentication
    ) {
        try {
            List<AddressResponse> addresses =
                    addressService.getAddresses(authentication.getName());

            return ResponseEntity.ok(addresses);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addAddress(
            @RequestBody Address address,
            Authentication authentication
    ) {
        try {
            AddressResponse savedAddress =
                    addressService.addAddress(
                            authentication.getName(),
                            address
                    );

            return ResponseEntity.ok(savedAddress);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long addressId,
            @RequestBody Address address,
            Authentication authentication
    ) {
        try {
            AddressResponse updatedAddress =
                    addressService.updateAddress(
                            authentication.getName(),
                            addressId,
                            address
                    );

            return ResponseEntity.ok(updatedAddress);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication
    ) {
        try {
            addressService.deleteAddress(
                    authentication.getName(),
                    addressId
            );

            return ResponseEntity.ok(
                    "Address deleted successfully"
            );

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}