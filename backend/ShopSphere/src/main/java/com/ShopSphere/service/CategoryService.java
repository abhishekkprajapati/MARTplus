package com.ShopSphere.service;

import com.ShopSphere.entity.Category;
import com.ShopSphere.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    // Create category
    public Category createCategory(Category category) {

        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException(
                    "Category already exists"
            );
        }

        category.setId(null);

        if (category.getStatus() == null ||
                category.getStatus().isBlank()) {

            category.setStatus("ACTIVE");
        }

        return categoryRepository.save(category);
    }

    // Get all categories
    public List<Category> getAllCategories() {

        return categoryRepository.findAll();
    }

    // Get category by ID
    public Category getCategoryById(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Category not found"
                        )
                );
    }

    // Update category
    public Category updateCategory(
            Long id,
            Category updatedCategory
    ) {

        Category existingCategory =
                getCategoryById(id);

        if (!existingCategory.getName()
                .equalsIgnoreCase(updatedCategory.getName())
                && categoryRepository.existsByName(
                        updatedCategory.getName()
                )) {

            throw new RuntimeException(
                    "Category already exists"
            );
        }

        existingCategory.setName(
                updatedCategory.getName()
        );

        existingCategory.setDescription(
                updatedCategory.getDescription()
        );

        if (updatedCategory.getStatus() != null &&
                !updatedCategory.getStatus().isBlank()) {

            existingCategory.setStatus(
                    updatedCategory.getStatus()
            );
        }

        return categoryRepository.save(
                existingCategory
        );
    }

    // Delete category
    public void deleteCategory(Long id) {

        Category category =
                getCategoryById(id);

        categoryRepository.delete(category);
    }
}
