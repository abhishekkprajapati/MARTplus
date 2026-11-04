package com.martplus.desktop.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic DAO Interface demonstrating Java Generics & OOP Interface abstraction.
 *
 * @param <T> Entity model type.
 * @param <ID> Primary key identifier type.
 */
public interface GenericDao<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    T save(T entity);

    void update(T entity);

    boolean deleteById(ID id);
}
