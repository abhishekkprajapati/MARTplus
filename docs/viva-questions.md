# Java GUI & JDBC Project Evaluation — Comprehensive Viva Guide

This document prepares the student for questions during the **Java GUI-Based Projects Review 1** viva voce examination.

---

## SECTION 1: OOP IMPLEMENTATION (10 MARKS)

### Q1: Where and how have you demonstrated Polymorphism?
**Answer:**
We demonstrated **Runtime Polymorphism** via the `DashboardView` interface and `RoleDashboardFactory`:
1. `DashboardView` defines a uniform interface for all dashboards (`initView()`, `refreshData()`, `getViewComponent()`, `onActivated()`).
2. At runtime, when a user logs in, `RoleDashboardFactory.createDashboard(user, mainWindow)` evaluates `user.getRole()` and dynamically instantiates either a `BuyerDashboard`, `SellerDashboard`, or `AdminDashboard`.
3. The calling host class (`MainWindow`) stores and manipulates this object exclusively through the `DashboardView` interface reference:
   ```java
   DashboardView dashboardView = RoleDashboardFactory.createDashboard(user, this);
   containerPanel.add(dashboardView.getViewComponent(), "DASHBOARD");
   dashboardView.onActivated();
   ```
*Code file to show:* `com.martplus.desktop.ui.RoleDashboardFactory`, `com.martplus.desktop.ui.DashboardView`, and `com.martplus.desktop.ui.MainWindow`.

---

### Q2: How did you implement Inheritance? Is it meaningful?
**Answer:**
Yes. We created an abstract superclass `BaseDashboard` (which extends `javax.swing.JPanel` and implements `DashboardView`).
- `BaseDashboard` centralizes shared desktop infrastructure: the navy sidebar, logo branding, user info pill, header bar, logout flow, and status bar.
- It defines an abstract template method:
  ```java
  protected abstract void buildNavigation(JPanel navPanel);
  ```
- Subclasses (`BuyerDashboard`, `SellerDashboard`, `AdminDashboard`) inherit layout and state management, providing only their role-specific navigation buttons and cards. This eliminates hundreds of lines of code duplication.
*Code file to show:* `com.martplus.desktop.ui.BaseDashboard` and `com.martplus.desktop.ui.BuyerDashboard`.

---

### Q3: How is Encapsulation maintained across your model layer?
**Answer:**
All entity fields in `com.martplus.desktop.model` (`User`, `Product`, `Order`, `CartItem`, etc.) are strictly `private`. Access is granted only through validated accessors and mutators:
- In `Product.java`, `setStock(int stock)` guards against negative values: `Math.max(0, stock)`.
- In `User.java`, `toString()` deliberately omits the password field so hashed credentials are never leaked to logs.
*Code file to show:* `com.martplus.desktop.model.Product` and `com.martplus.desktop.model.User`.

---

### Q4: Explain your custom exception hierarchy.
**Answer:**
We constructed a clean OOP exception tree rooted at `ApplicationException` (extends `RuntimeException`):
- `DatabaseException`: Handles JDBC connectivity failures, SQL syntax errors, and constraint violations.
- `AuthenticationException`: Handles bad credentials, account deactivations, and role access violations.
- `ValidationException`: Handles illegal user input (negative stock, empty fields, bad email format).
Exceptions are never swallowed; they propagate up and are converted into human-readable Swing dialogs via `DialogUtil.showError()`.
*Code file to show:* `com.martplus.desktop.exception.ApplicationException`.

---

## SECTION 2: COLLECTIONS & GENERICS (6 MARKS)

### Q5: How and why are Generics used in your DAO layer?
**Answer:**
We implemented the Generic DAO Pattern using `<T, ID>`:
- `GenericDao<T, ID>` defines generic CRUD operations: `Optional<T> findById(ID id)`, `List<T> findAll()`, `T save(T entity)`.
- `AbstractJdbcDao<T, ID>` implements this interface and encapsulates generic parameterized execution: `executeQueryForList(String sql, Object... params)`.
- Concrete subclasses specify their types: `JdbcUserDao extends AbstractJdbcDao<User, Long> implements UserDao`.
*Code file to show:* `com.martplus.desktop.dao.GenericDao` and `com.martplus.desktop.dao.AbstractJdbcDao`.

---

### Q6: Where do you use `List`, `Set`, and `Map` in practical business features?
**Answer:**
1. **`List`:** `List<Product>` in `ProductPanel` to display ordered search results in the table; `List<CartItem>` in `OrderService` for sequentially processing order lines.
2. **`Set`:** `Set<String>` (TreeSet) in `ProductService.getUniqueCategoryNames()` to filter out duplicate categories and sort them case-insensitively for the JComboBox dropdown.
3. **`Map`:**
   - `Map<Long, Product>` in `ProductService.getProductMap()` for $O(1)$ product ID cache lookups.
   - `ConcurrentHashMap<Long, ReentrantLock>` in `InventoryLockManager` for fine-grained per-product locking.
*Code file to show:* `com.martplus.desktop.service.ProductService` and `com.martplus.desktop.concurrency.InventoryLockManager`.

---

## SECTION 3: MULTITHREADING & SYNCHRONIZATION (4 MARKS)

### Q7: Why can't you run JDBC queries directly inside a JButton's ActionListener?
**Answer:**
In Java Swing, all UI events and painting occur on a single thread: the **Event Dispatch Thread (EDT)**. If a slow JDBC network query runs on the EDT, the entire window freezes, buttons stop responding, and the operating system reports the application as "Not Responding".
To prevent this, we execute database queries off the EDT using `AsyncOperation` (which extends `SwingWorker`) and `BackgroundTaskManager` (which manages a `ThreadPoolExecutor`), dispatching only the finished results back to the EDT for table updates.
*Code file to show:* `com.martplus.desktop.concurrency.AsyncOperation`.

---

### Q8: How do you prevent race conditions when multiple users buy the same limited stock?
**Answer:**
We employ a two-tiered synchronization strategy:
1. **Application Layer (Java):** `InventoryLockManager` maintains fair `ReentrantLock` instances per product inside a `ConcurrentHashMap`. Any thread adjusting stock must acquire that product's lock first:
   ```java
   InventoryLockManager.runWithLock(productId, () -> { ... });
   ```
2. **Database Layer (PostgreSQL):** In `OrderService.processCheckout`, we execute:
   ```sql
   SELECT * FROM products WHERE id = ? FOR UPDATE
   ```
   This places an exclusive row-level lock in PostgreSQL for the duration of the active transaction.
*Code file to show:* `com.martplus.desktop.concurrency.InventoryLockManager` and `com.martplus.desktop.InventoryLockManagerTest`.

---

## SECTION 4: DATABASE OPERATIONS & JDBC (7 + 3 MARKS)

### Q9: How do you establish database connections without Spring or Hibernate?
**Answer:**
We use direct JDBC via `java.sql.DriverManager.getConnection(url, user, password)` inside `DatabaseConfig.java`. It reads environment variables (`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`) with safe defaults, dynamically loads `org.postgresql.Driver`, and returns fresh, independent connections.
*Code file to show:* `com.martplus.desktop.config.DatabaseConfig`.

---

### Q10: How do you protect the application from SQL Injection?
**Answer:**
Every SQL statement in our DAO layer uses parameterized `PreparedStatement` with `?` placeholders. No user-supplied strings are ever concatenated directly into SQL text.
*Code file to show:* `com.martplus.desktop.dao.JdbcProductDao` or `com.martplus.desktop.dao.AbstractJdbcDao`.

---

### Q11: Walk me through your atomic checkout transaction.
**Answer:**
In `OrderService.processCheckout()`, we coordinate 12 steps atomically:
1. Call `conn.setAutoCommit(false)` to begin the transaction.
2. For each cart item, execute `SELECT * FROM products WHERE id = ? FOR UPDATE` to lock the stock row.
3. Validate active status and ensure `product.getStock() >= requestedQty`.
4. Calculate order total and execute `INSERT INTO orders VALUES (...) RETURNING id`.
5. Execute batch `INSERT INTO order_items`.
6. Decrement stock: `UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?`.
7. Clear buyer's cart: `DELETE FROM cart_items WHERE buyer_id = ?`.
8. Insert tracking milestone: `INSERT INTO order_tracking VALUES (...)`.
9. Call `conn.commit()`.
10. If any step throws an exception, the `catch` block calls `conn.rollback()`.
11. The `finally` block restores `conn.setAutoCommit(true)` and closes the connection.
*Code file to show:* `com.martplus.desktop.service.OrderService.processCheckout()`.

---

## SECTION 5: SECURITY & COMPATIBILITY

### Q12: How does your desktop application verify passwords against the existing Spring Boot database?
**Answer:**
The web application hashes passwords using Spring Security's `BCryptPasswordEncoder`. In `PasswordUtil.java`, we use BCrypt hashing. When reading the stored hash from PostgreSQL, we handle both `$2a$` and `$2b$` salts, allowing direct, seamless verification of existing web user accounts without modifying the database.
*Code file to show:* `com.martplus.desktop.util.PasswordUtil.java` and `com.martplus.desktop.PasswordUtilTest`.
