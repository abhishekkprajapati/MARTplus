# MARTplus — Java GUI Desktop Application

A modern, high-performance desktop application for **MARTplus** (Multi-Vendor E-Commerce Platform) built with **Java 21 Swing** and direct **JDBC database connectivity** to PostgreSQL.

This application is designed as the primary academic implementation fulfilling the **Java GUI-Based Projects Review 1 marking rubric**, while fully preserving the existing MARTplus web frontend and Spring Boot backend.

---

## 🎯 Architecture & Technologies

- **Language:** Java 21 LTS
- **UI Framework:** Java Swing (with FlatLaf modern UI engine)
- **Database Engine:** PostgreSQL (shared with web platform)
- **Data Access:** Pure JDBC (`Connection`, `PreparedStatement`, `ResultSet`, `DriverManager`)
- **Password Security:** BCrypt password hashing (100% interoperable with Spring Security)
- **Concurrency:** `ExecutorService`, `SwingWorker`, `ReentrantLock`, `ConcurrentHashMap`
- **Testing:** JUnit 5 Jupiter, Mockito, Dynamic Reflection Proxies

---

## 📁 Package Layout

```
com.martplus.desktop/
├── Main.java                        # Application entry point on Swing EDT
├── config/
│   ├── DatabaseConfig.java          # Direct JDBC DriverManager connection factory
│   └── AppConfig.java               # Global application state & authenticated user session
├── model/
│   ├── User.java                    # Encapsulated user entity (Buyer, Seller, Admin)
│   ├── Product.java                 # Catalog product entity with stock rules
│   ├── Category.java                # Product category
│   ├── CartItem.java                # Shopping cart item with subtotal calculation
│   ├── WishlistItem.java            # Saved wishlist record
│   ├── Address.java                 # Buyer delivery address
│   ├── Order.java                   # Aggregate order with item collection
│   ├── OrderItem.java               # Order line item with price snapshot
│   ├── Payment.java                 # Transaction record
│   ├── OrderTracking.java           # Tracking milestone
│   └── ActivityLog.java             # Admin audit trail
├── dao/
│   ├── GenericDao.java              # Generic DAO interface (Generics & OOP Interface)
│   ├── AbstractJdbcDao.java         # Base generic JDBC DAO with PreparedStatement helpers
│   ├── UserDao.java & JdbcUserDao.java
│   ├── ProductDao.java & JdbcProductDao.java
│   ├── CategoryDao.java & JdbcCategoryDao.java
│   ├── CartDao.java & JdbcCartDao.java
│   ├── WishlistDao.java & JdbcWishlistDao.java
│   ├── AddressDao.java & JdbcAddressDao.java
│   ├── OrderDao.java & JdbcOrderDao.java
│   ├── InventoryDao.java & JdbcInventoryDao.java
│   └── ActivityLogDao.java & JdbcActivityLogDao.java
├── service/
│   ├── AuthenticationService.java   # BCrypt login verification against PostgreSQL
│   ├── UserService.java             # User management, role access, status controls
│   ├── ProductService.java          # Product search, catalog filtering, ownership checks
│   ├── CartService.java             # Cart operations, quantity updates, total calculation
│   ├── WishlistService.java         # Wishlist management
│   ├── OrderService.java            # Atomic 12-step checkout JDBC transaction management
│   ├── InventoryService.java        # Synchronized inventory stock updates with ReentrantLock
│   └── ActivityService.java         # Audit logging for administrative actions
├── ui/
│   ├── DashboardView.java           # Polymorphic dashboard interface
│   ├── BaseDashboard.java           # Abstract base class (Inheritance, navigation, status)
│   ├── RoleDashboardFactory.java    # Dynamic factory returning polymorphic DashboardView
│   ├── MainWindow.java              # Main JFrame hosting login & dynamic dashboards
│   ├── LoginFrame.java              # Swing login screen connecting to PostgreSQL
│   ├── BuyerDashboard.java          # Buyer dashboard implementation
│   ├── SellerDashboard.java         # Seller dashboard implementation
│   ├── AdminDashboard.java          # Admin governance dashboard
│   ├── ProductPanel.java            # Product browsing & search table
│   ├── CartPanel.java               # Cart table & transaction checkout dialog
│   ├── WishlistPanel.java           # Wishlist table & move-to-cart
│   ├── OrderPanel.java              # Order history, item details & tracking milestones
│   ├── InventoryPanel.java          # Seller inventory control & low-stock alerts
│   ├── UserManagementPanel.java     # Admin user administration (passwords protected)
│   ├── ProductManagementPanel.java  # Product CRUD & active/inactive toggles
│   └── ActivityLogPanel.java        # Administrative audit trail table
├── controller/
│   ├── LoginController.java
│   ├── BuyerController.java
│   ├── SellerController.java
│   └── AdminController.java
├── concurrency/
│   ├── BackgroundTaskManager.java   # Custom ThreadPoolExecutor with daemon threads
│   ├── InventoryLockManager.java    # Fine-grained per-product ReentrantLocks & ConcurrentHashMap
│   └── AsyncOperation.java          # SwingWorker abstraction keeping EDT responsive
├── exception/
│   ├── ApplicationException.java    # Base application exception
│   ├── DatabaseException.java       # JDBC & SQL errors
│   ├── AuthenticationException.java # Credential & authorization errors
│   └── ValidationException.java     # Input & stock validation errors
└── util/
    ├── PasswordUtil.java            # BCrypt hashing & cross-platform hash matching
    ├── ValidationUtil.java          # Email, phone, price, and stock validation
    ├── DialogUtil.java              # Standardized Swing modal dialogs
    ├── DateUtil.java                # Timestamp formatting
    └── UITheme.java                 # Modern MARTplus brand design system
```

---

## 🛠️ Prerequisites & Setup

1. **Java Development Kit (JDK 21 or newer)**
2. **Apache Maven 3.9+**
3. **PostgreSQL Database** running with the existing MARTplus (`shopsphere`) schema.

### Environment Variables

Configure the following environment variables (or rely on local defaults):

```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/shopsphere"
export DATABASE_USERNAME="shopsphere_user"
export DATABASE_PASSWORD="your_password"
```

---

## 🚀 Build and Run

### Run Full Test Suite & Package

```bash
mvn -f desktop-app/pom.xml clean verify
```

### Launch the Desktop Application

```bash
mvn -f desktop-app/pom.xml exec:java -Dexec.mainClass=com.martplus.desktop.Main
```

Alternatively, run the packaged JAR:

```bash
java -jar desktop-app/target/martplus-desktop-1.0.0.jar
```
