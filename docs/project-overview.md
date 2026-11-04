# MARTplus — Project Overview & Academic Review 1 Compliance

## 1. Project Background
**MARTplus** is an enterprise-grade multi-vendor e-commerce platform. While the production web environment utilizes a modern Spring Boot + PostgreSQL web architecture, academic evaluation under the **Java GUI-Based Projects Marking Rubric** necessitates a native desktop application built with:
- Pure **Java 21 Swing**
- Direct **JDBC Database Connectivity** (no ORM or Spring Data abstraction)
- Robust **Core Java OOP Principles** (Encapsulation, Inheritance, Polymorphism, Interfaces, Exception Handling)
- Real **Java Collections & Generics**
- Real **Multithreading & Thread Synchronization**
- Atomic **Multi-step JDBC Transactions**

This desktop application was engineered in `desktop-app/` as a modular addition to the MARTplus repository, leaving existing backend APIs, web frontend assets, and database schemas completely intact.

---

## 2. Rubric Mapping Summary

| Review 1 Assessment Rubric | Mark Weight | Primary Implementation Packages & Classes | Verification Tests |
| :--- | :---: | :--- | :--- |
| **OOP Implementation** (Polymorphism, Inheritance, Exception Handling, Interfaces) | **10 Marks** | `BaseDashboard`, `DashboardView`, `RoleDashboardFactory`, `GenericDao<T,ID>`, `ApplicationException` hierarchy | `RoleDashboardFactoryTest`, `GenericDaoTest` |
| **Collections & Generics** | **6 Marks** | `GenericDao<T,ID>`, `AbstractJdbcDao<T,ID>`, `List<Product>`, `Set<String>`, `Map<Long,Product>`, `List<CartItem>` | `GenericDaoTest`, `StockValidationTest` |
| **Multithreading & Synchronization** | **4 Marks** | `BackgroundTaskManager` (ThreadPoolExecutor), `InventoryLockManager` (`ReentrantLock`, `ConcurrentHashMap`), `AsyncOperation` (`SwingWorker`) | `InventoryLockManagerTest` |
| **Classes for Database Operations** | **7 Marks** | `AbstractJdbcDao`, `JdbcUserDao`, `JdbcProductDao`, `JdbcCartDao`, `JdbcOrderDao`, `JdbcInventoryDao`, `JdbcAddressDao`, `JdbcActivityLogDao` | `DataIsolationTest`, `CheckoutTransactionTest` |
| **Database Connectivity (JDBC)** | **3 Marks** | `DatabaseConfig`, `DriverManager.getConnection()`, `PreparedStatement`, `ResultSet`, try-with-resources | `PasswordUtilTest`, `CheckoutTransactionTest` |
| **Implement JDBC Connectivity** (Transaction Management) | **3 Marks** | `OrderService.processCheckout`: atomic 12-step checkout (`setAutoCommit(false)`, `commit()`, `rollback()`, `SELECT FOR UPDATE`) | `CheckoutTransactionTest` |
| **Total Review 1 Scope** | **33 Marks** | **All 6 Criteria 100% Implemented & Tested** | **22 Tests Passing (0 Failures)** |

---

## 3. Brand Identity & Swing Design System
- **Desktop Window Dimensions:** Default $1280 \times 800$, minimum $1024 \times 680$, fully responsive.
- **Brand Colors:**
  - Deep Navy Sidebar: `#1E293B`
  - Slate Background: `#F8FAFC`
  - White Cards: `#FFFFFF` with rounded borders (`#E2E8F0`)
  - Indigo / Purple Primary Accents: `#4F46E5` / `#7C3AED`
  - Emerald Green Status Indicators: `#10B981`
- **Typography:** Segoe UI typography hierarchy (`FONT_TITLE`, `FONT_SUBTITLE`, `FONT_BODY`, `FONT_BODY_BOLD`, `FONT_SMALL`).
- **UI Safety:** Heavy database reads/writes run on dedicated background threads via `AsyncOperation`, keeping the Swing Event Dispatch Thread (EDT) completely fluid.
