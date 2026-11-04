# Core Java OOP Implementation (10 Marks Compliance)

This document provides technical proof of how all required Object-Oriented Programming (OOP) concepts are implemented and demonstrated in the MARTplus codebase.

---

## 1. Encapsulation

Encapsulation bundles data (fields) and methods operating on that data within a class while hiding internal state from arbitrary outside interference.

### Concrete Implementation:
- **`com.martplus.desktop.model.User`**:
  - All instance variables (`id`, `firstName`, `lastName`, `email`, `phone`, `password`, `role`, `status`) are declared `private`.
  - Access is provided only through explicit getters and setters.
  - The sensitive `password` field (which stores the BCrypt hash) is protected: `toString()` specifically omits the password field to prevent credential leakage in logging.
- **`com.martplus.desktop.model.Product`**:
  - `price` and `stock` fields are encapsulated with defensive bounds checks:
    ```java
    public void setStock(Integer stock) {
        this.stock = stock != null ? Math.max(0, stock) : 0;
    }
    ```
- **`com.martplus.desktop.model.OrderItem`**:
  - `subtotal` is encapsulated and automatically recalculated whenever `price` or `quantity` is modified.

---

## 2. Inheritance

Inheritance allows a new class to acquire properties and behaviors of an existing class, promoting reusability and structured polymorphism.

### Concrete Implementation:
- **`com.martplus.desktop.ui.BaseDashboard`** (Abstract Superclass):
  - Extends `javax.swing.JPanel`.
  - Manages the common layout structure: left navy sidebar, top brand header, user info pill, logout handler, content area with `CardLayout`, and bottom database connectivity status bar.
  - Declares the abstract template method:
    ```java
    protected abstract void buildNavigation(JPanel navPanel);
    ```
- **Subclasses Extending `BaseDashboard`**:
  - `com.martplus.desktop.ui.BuyerDashboard`: Inherits the sidebar, header, and card layout; implements `buildNavigation()` to add Buyer buttons (Browse, Cart, Wishlist, Orders, Addresses).
  - `com.martplus.desktop.ui.SellerDashboard`: Inherits layout; implements `buildNavigation()` to add Seller buttons (Overview, Products, Inventory, Orders).
  - `com.martplus.desktop.ui.AdminDashboard`: Inherits layout; implements `buildNavigation()` to add Admin buttons (Overview, Users, Catalog, Orders, Logs).

---

## 3. Polymorphism

Polymorphism enables an object to take many forms, allowing a caller to treat different underlying implementations uniformly through an interface or superclass reference.

### Concrete Implementation:
- **Interface Reference:** `com.martplus.desktop.ui.DashboardView` defines the common lifecycle contract (`initView()`, `refreshData()`, `getViewComponent()`, `onActivated()`, `onDeactivated()`).
- **Dynamic Factory:** `com.martplus.desktop.ui.RoleDashboardFactory`:
  ```java
  public class RoleDashboardFactory {
      public static DashboardView createDashboard(User user, MainWindow mainWindow) {
          return switch (user.getRole().toUpperCase()) {
              case "BUYER"  -> new BuyerDashboard(mainWindow, user);
              case "SELLER" -> new SellerDashboard(mainWindow, user);
              case "ADMIN"  -> new AdminDashboard(mainWindow, user);
              default -> throw new AuthenticationException("Unknown role: " + user.getRole());
          };
      }
  }
  ```
- **Runtime Polymorphism in `MainWindow`:**
  ```java
  DashboardView dashboardView = RoleDashboardFactory.createDashboard(user, this);
  // Treated entirely through DashboardView polymorphic reference
  containerPanel.add(dashboardView.getViewComponent(), "DASHBOARD");
  dashboardView.onActivated();
  ```

---

## 4. Interfaces

Interfaces define strict contracts specifying *what* an implementing class must do without dictating *how* it does it.

### Concrete Implementation:
- **`com.martplus.desktop.dao.GenericDao<T, ID>`**:
  - Defines the generic contract for data access operations: `Optional<T> findById(ID id)`, `List<T> findAll()`, `T save(T entity)`, `void update(T entity)`, `boolean deleteById(ID id)`.
- **Specialized DAO Interfaces**:
  - `UserDao extends GenericDao<User, Long>`
  - `ProductDao extends GenericDao<Product, Long>`
  - `CartDao extends GenericDao<CartItem, Long>`
  - `OrderDao extends GenericDao<Order, Long>`
  - `WishlistDao extends GenericDao<WishlistItem, Long>`
  - `AddressDao extends GenericDao<Address, Long>`
  - `InventoryDao`
  - `ActivityLogDao extends GenericDao<ActivityLog, Long>`
- **View Interface**:
  - `DashboardView`

---

## 5. Exception Handling

Robust exception handling ensures unexpected conditions (network failures, invalid inputs, duplicate keys) are handled gracefully without application crashes or security leaks.

### Custom Exception Hierarchy:
```
java.lang.Throwable
 └── java.lang.Exception
      └── java.lang.RuntimeException
           └── ApplicationException (Root Desktop Exception)
                ├── DatabaseException       (JDBC errors, connection timeouts, constraint violations)
                ├── AuthenticationException (Invalid password, inactive account, role access denial)
                └── ValidationException     (Negative stock, invalid email, empty required fields)
```

### Key Techniques Demonstrated:
1. **try-with-resources:** Used across all JDBC DAOs to automatically close `Connection`, `PreparedStatement`, and `ResultSet` even if exceptions occur.
2. **Transaction Rollback in catch blocks:** In `OrderService.processCheckout`, any failure automatically triggers `conn.rollback()` before releasing resources.
3. **Never swallow exceptions:** Every catch block logs the root cause and wraps SQLExceptions in meaningful `DatabaseException` messages for user presentation.
