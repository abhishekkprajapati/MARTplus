package com.martplus.desktop.dao;

import com.martplus.desktop.exception.DatabaseException;
import com.martplus.desktop.model.Address;

import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of AddressDao for PostgreSQL 'addresses' table.
 */
public class JdbcAddressDao extends AbstractJdbcDao<Address, Long> implements AddressDao {

    @Override
    protected Address mapResultSet(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new Address(
                rs.getLong("id"),
                rs.getLong("buyer_id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("address_line"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("pincode"),
                rs.getBoolean("default_address"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null
        );
    }

    @Override
    public Optional<Address> findById(Long id) {
        String sql = "SELECT * FROM addresses WHERE id = ?";
        return executeQueryForSingle(sql, id);
    }

    @Override
    public List<Address> findAll() {
        String sql = "SELECT * FROM addresses ORDER BY id DESC";
        return executeQueryForList(sql);
    }

    @Override
    public List<Address> findByBuyerId(Long buyerId) {
        String sql = "SELECT * FROM addresses WHERE buyer_id = ? ORDER BY default_address DESC, id DESC";
        return executeQueryForList(sql, buyerId);
    }

    @Override
    public Optional<Address> findByIdAndBuyerId(Long id, Long buyerId) {
        String sql = "SELECT * FROM addresses WHERE id = ? AND buyer_id = ?";
        return executeQueryForSingle(sql, id, buyerId);
    }

    @Override
    public Optional<Address> findDefaultAddress(Long buyerId) {
        String sql = "SELECT * FROM addresses WHERE buyer_id = ? AND default_address = true LIMIT 1";
        return executeQueryForSingle(sql, buyerId);
    }

    @Override
    public void resetDefaultAddress(Long buyerId) {
        String sql = "UPDATE addresses SET default_address = false, updated_at = ? WHERE buyer_id = ?";
        executeUpdate(sql, java.time.LocalDateTime.now(), buyerId);
    }

    @Override
    public void setDefaultAddress(Long addressId, Long buyerId) {
        resetDefaultAddress(buyerId);
        String sql = "UPDATE addresses SET default_address = true, updated_at = ? WHERE id = ? AND buyer_id = ?";
        executeUpdate(sql, java.time.LocalDateTime.now(), addressId, buyerId);
    }

    @Override
    public Address save(Address address) {
        if (address.isDefaultAddress()) {
            resetDefaultAddress(address.getBuyerId());
        }

        String sql = "INSERT INTO addresses (buyer_id, full_name, phone, address_line, city, state, pincode, default_address, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            setParameters(stmt,
                    address.getBuyerId(),
                    address.getFullName(),
                    address.getPhone(),
                    address.getAddressLine(),
                    address.getCity(),
                    address.getState(),
                    address.getPincode(),
                    address.isDefaultAddress(),
                    address.getCreatedAt() != null ? address.getCreatedAt() : now,
                    now
            );

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    address.setId(rs.getLong("id"));
                }
            }
            return address;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save address: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Address address) {
        if (address.isDefaultAddress()) {
            resetDefaultAddress(address.getBuyerId());
        }

        String sql = "UPDATE addresses SET full_name = ?, phone = ?, address_line = ?, city = ?, state = ?, pincode = ?, default_address = ?, updated_at = ? " +
                     "WHERE id = ? AND buyer_id = ?";
        executeUpdate(sql,
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.isDefaultAddress(),
                java.time.LocalDateTime.now(),
                address.getId(),
                address.getBuyerId()
        );
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM addresses WHERE id = ?";
        return executeUpdate(sql, id) > 0;
    }
}
