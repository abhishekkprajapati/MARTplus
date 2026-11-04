package com.martplus.desktop.dao;

import com.martplus.desktop.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object contract for Product Categories.
 */
public interface CategoryDao extends GenericDao<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findActiveCategories();
}
