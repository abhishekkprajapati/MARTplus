package com.ShopSphere.repository;

import com.ShopSphere.entity.Address;
import com.ShopSphere.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByBuyer(User buyer);

    Optional<Address> findByIdAndBuyer(Long id, User buyer);

    Optional<Address> findByBuyerAndDefaultAddressTrue(User buyer);
}