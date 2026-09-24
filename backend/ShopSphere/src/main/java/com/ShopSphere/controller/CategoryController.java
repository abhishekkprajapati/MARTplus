package com.ShopSphere.controller;

import com.ShopSphere.entity.Category;
import com.ShopSphere.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(
            CategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    // Get all categories - Public
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {

        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );
    }

    // Get category by ID - Public
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(
            @PathVariable Long id
    ) {

        try {

            return ResponseEntity.ok(
                    categoryService.getCategoryById(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // Create category - Admin
    @PostMapping
    public ResponseEntity<?> createCategory(
            @RequestBody Category category
    ) {

        try {

            Category savedCategory =
                    categoryService.createCategory(category);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedCategory);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Update category - Admin
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category
    ) {

        try {

            Category updatedCategory =
                    categoryService.updateCategory(
                            id,
                            category
                    );

            return ResponseEntity.ok(
                    updatedCategory
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Delete category - Admin
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(
            @PathVariable Long id
    ) {

        try {

            categoryService.deleteCategory(id);

            return ResponseEntity.ok(
                    "Category deleted successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
