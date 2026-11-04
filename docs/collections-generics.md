# Collections & Generics (6 Marks Compliance)

This document details the practical usage and architectural necessity of Java Collections and Generics within the MARTplus desktop application.

---

## 1. Java Generics

Generics provide compile-time type safety, eliminating runtime `ClassCastException` and enabling reusable algorithms across different data models.

### Demonstrated Generic Types:

1. **`GenericDao<T, ID>`**:
   ```java
   public interface GenericDao<T, ID> {
       Optional<T> findById(ID id);
       List<T> findAll();
       T save(T entity);
       void update(T entity);
       boolean deleteById(ID id);
   }
   ```
   - `<T>` represents the entity model (`User`, `Product`, `Order`, `CartItem`, etc.).
   - `<ID>` represents the primary key type (`Long`).

2. **`AbstractJdbcDao<T, ID>`**:
   - Implements `GenericDao<T, ID>` and provides reusable generic query methods:
     - `protected List<T> executeQueryForList(String sql, Object... params)`
     - `protected Optional<T> executeQueryForSingle(String sql, Object... params)`
     - `protected abstract T mapResultSet(ResultSet rs) throws SQLException`
   - Every specialized JDBC DAO (`JdbcUserDao`, `JdbcProductDao`, `JdbcOrderDao`) reuses this generic foundation.

3. **`AsyncOperation<T>` (Extends `SwingWorker<T, Void>`)**:
   - Represents a generic background operation off the EDT, parameterized by the return type of the database computation.

---

## 2. Java Collections

### 1. `java.util.List<E>`
- **`List<Product>`**: Used in `ProductService.searchProducts()` and `ProductPanel` to hold and display ordered catalog products.
- **`List<CartItem>`**: Used in `CartService.getCartItems()` and `OrderService.processCheckout()` to iterate sequentially over items during total amount calculations and inventory locking.
- **`List<Order>`**: Used in `OrderPanel` to display buyer and seller order histories.
- **`List<OrderItem>`**: Aggregated within `Order.getItems()` representing order line items.

### 2. `java.util.Set<E>`
- **`Set<String>` (TreeSet with `String.CASE_INSENSITIVE_ORDER`)**:
  - Implemented in `ProductService.getUniqueCategoryNames()`.
  - Ensures category dropdown options contain unique, sorted category names without case-sensitivity duplicates.

### 3. `java.util.Map<K, V>`
- **`Map<Long, Product>` (`HashMap<Long, Product>`)**:
  - Implemented in `ProductService.getProductMap(List<Long> productIds)`.
  - Enables $O(1)$ fast lookup of products by their primary key ID.
- **`Map<String, JButton>`**:
  - Implemented in `BaseDashboard` to track sidebar navigation buttons and dynamically highlight the active view button.

### 4. `java.util.concurrent.ConcurrentHashMap<K, V>`
- Implemented in `InventoryLockManager`:
  ```java
  private static final ConcurrentHashMap<Long, ReentrantLock> PRODUCT_LOCKS = new ConcurrentHashMap<>();
  ```
  - Provides lock-free concurrent reads and atomic lock creation per product ID for multithreaded synchronization.
