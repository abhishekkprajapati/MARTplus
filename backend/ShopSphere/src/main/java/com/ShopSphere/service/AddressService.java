package com.ShopSphere.service;

import com.ShopSphere.dto.AddressResponse;
import com.ShopSphere.entity.Address;
import com.ShopSphere.entity.User;
import com.ShopSphere.repository.AddressRepository;
import com.ShopSphere.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(
            AddressRepository addressRepository,
            UserRepository userRepository
    ) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    private User getBuyer(String email) {
        return userRepository.findByEmailAndRole(email, "BUYER")
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
    }

    public List<AddressResponse> getAddresses(String buyerEmail) {
        User buyer = getBuyer(buyerEmail);

        return addressRepository.findByBuyer(buyer)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AddressResponse addAddress(
            String buyerEmail,
            Address address
    ) {
        User buyer = getBuyer(buyerEmail);

        address.setId(null);
        address.setBuyer(buyer);

        if (address.getDefaultAddress() == null) {
            address.setDefaultAddress(false);
        }

        List<Address> existing = addressRepository.findByBuyer(buyer);
        if (existing.isEmpty()) {
            address.setDefaultAddress(true);
        } else if (address.getDefaultAddress()) {
            removeCurrentDefault(buyer);
        }

        Address savedAddress = addressRepository.save(address);

        return toResponse(savedAddress);
    }

    public AddressResponse updateAddress(
            String buyerEmail,
            Long addressId,
            Address updatedAddress
    ) {
        User buyer = getBuyer(buyerEmail);

        Address address = addressRepository
                .findByIdAndBuyer(addressId, buyer)
                .orElseThrow(() ->
                        new RuntimeException("Address not found")
                );

        address.setFullName(updatedAddress.getFullName());
        address.setPhone(updatedAddress.getPhone());
        address.setAddressLine(updatedAddress.getAddressLine());
        address.setCity(updatedAddress.getCity());
        address.setState(updatedAddress.getState());
        address.setPincode(updatedAddress.getPincode());

        Boolean makeDefault = updatedAddress.getDefaultAddress();

        if (makeDefault == null) {
            makeDefault = false;
        }

        if (makeDefault) {
            removeCurrentDefault(buyer);
        }

        address.setDefaultAddress(makeDefault);

        Address savedAddress = addressRepository.save(address);

        return toResponse(savedAddress);
    }

    public void deleteAddress(
            String buyerEmail,
            Long addressId
    ) {
        User buyer = getBuyer(buyerEmail);

        Address address = addressRepository
                .findByIdAndBuyer(addressId, buyer)
                .orElseThrow(() ->
                        new RuntimeException("Address not found")
                );

        boolean wasDefault = Boolean.TRUE.equals(address.getDefaultAddress());
        addressRepository.delete(address);

        if (wasDefault) {
            List<Address> remaining = addressRepository.findByBuyer(buyer);
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setDefaultAddress(true);
                addressRepository.save(newDefault);
            }
        }
    }

    private void removeCurrentDefault(User buyer) {
        addressRepository
                .findByBuyerAndDefaultAddressTrue(buyer)
                .ifPresent(address -> {
                    address.setDefaultAddress(false);
                    addressRepository.save(address);
                });
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getDefaultAddress(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}