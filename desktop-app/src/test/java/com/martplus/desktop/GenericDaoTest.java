package com.martplus.desktop;

import com.martplus.desktop.dao.GenericDao;
import com.martplus.desktop.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Collections, Generics & DAO Abstraction Tests")
public class GenericDaoTest {

    /**
     * In-memory concrete implementation of GenericDao<Category, Long>
     * demonstrating Generic type parameters and collection manipulation.
     */
    static class InMemoryCategoryDao implements GenericDao<Category, Long> {
        private final Map<Long, Category> storage = new LinkedHashMap<>();
        private long idSequence = 1L;

        @Override
        public Optional<Category> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Category> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public Category save(Category entity) {
            if (entity.getId() == null) {
                entity.setId(idSequence++);
            }
            storage.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public void update(Category entity) {
            if (entity.getId() != null && storage.containsKey(entity.getId())) {
                storage.put(entity.getId(), entity);
            }
        }

        @Override
        public boolean deleteById(Long id) {
            return storage.remove(id) != null;
        }
    }

    @Test
    @DisplayName("Should demonstrate GenericDao contract with type safety")
    void testGenericDaoOperations() {
        GenericDao<Category, Long> dao = new InMemoryCategoryDao();

        Category c1 = dao.save(new Category(null, "Electronics", "Gadgets", "ACTIVE", LocalDateTime.now()));
        Category c2 = dao.save(new Category(null, "Fashion", "Apparel", "ACTIVE", LocalDateTime.now()));

        assertNotNull(c1.getId());
        assertNotNull(c2.getId());

        List<Category> all = dao.findAll();
        assertEquals(2, all.size(), "List<Category> must contain 2 items");

        Optional<Category> found = dao.findById(c1.getId());
        assertTrue(found.isPresent());
        assertEquals("Electronics", found.get().getName());

        c1.setName("Consumer Electronics");
        dao.update(c1);
        assertEquals("Consumer Electronics", dao.findById(c1.getId()).get().getName());

        boolean deleted = dao.deleteById(c2.getId());
        assertTrue(deleted);
        assertEquals(1, dao.findAll().size());
    }

    @Test
    @DisplayName("Should demonstrate Collections operations: filtering, uniqueness, and lookup")
    void testCollectionsDemonstrations() {
        List<String> rawCategories = List.of("Electronics", "Fashion", "electronics", "Books", "Fashion");

        // Set demonstration: case-insensitive uniqueness
        Set<String> uniqueCategories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        uniqueCategories.addAll(rawCategories);
        assertEquals(3, uniqueCategories.size(), "Set must eliminate case-insensitive duplicates");
        assertTrue(uniqueCategories.contains("Electronics"));
        assertTrue(uniqueCategories.contains("Fashion"));
        assertTrue(uniqueCategories.contains("Books"));

        // Map demonstration: keyed lookup
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (String c : rawCategories) {
            String normalized = c.toLowerCase();
            categoryCounts.put(normalized, categoryCounts.getOrDefault(normalized, 0) + 1);
        }

        assertEquals(2, categoryCounts.get("electronics"));
        assertEquals(2, categoryCounts.get("fashion"));
        assertEquals(1, categoryCounts.get("books"));
    }
}
