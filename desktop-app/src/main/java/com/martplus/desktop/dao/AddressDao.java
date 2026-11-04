package com.martplus.desktop.dao;

import com.martplus.desktop.model.Address;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object contract for delivery Addresses.
 */
public interface AddressDao extends GenericDao<Address, Long> {

    List<Address> findByBuyerId(Long buyerId);

    Optional<Address> findByIdAndBuyerId(Long id, Long buyerId);

    Optional<Address> findDefaultAddress(Long buyerId);

    void resetDefaultAddress(Long buyerId);

    void setDefaultAddress(Long addressId, Long buyerId);
}
