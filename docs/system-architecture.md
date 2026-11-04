# MARTplus Desktop — System Architecture

## 1. Architectural Layers

The desktop application enforces strict Model-View-Controller (MVC) and Data Access Object (DAO) separation:

```
┌────────────────────────────────────────────────────────┐
│             Presentation Layer (Java Swing)            │
│  MainWindow, LoginFrame, BaseDashboard, Child Panels   │
└───────────────────────────┬────────────────────────────┘
                            │ Dispatches User Events
┌───────────────────────────▼────────────────────────────┐
│                  Controller Layer                      │
│ LoginController, BuyerController, SellerController...  │
└───────────────────────────┬────────────────────────────┘
                            │ Delegates Business Rules
┌───────────────────────────▼────────────────────────────┐
│                    Service Layer                       │
│ AuthenticationService, OrderService, InventoryService..│
│ (Coordinates Concurrency, Locks & Transactions)        │
└───────────────────────────┬────────────────────────────┘
                            │ Direct JDBC Data Access
┌───────────────────────────▼────────────────────────────┐
│                   DAO / JDBC Layer                     │
│ GenericDao, AbstractJdbcDao, JdbcUserDao, JdbcOrderDao │
└───────────────────────────┬────────────────────────────┘
                            │ Parameterized PreparedStatements
┌───────────────────────────▼────────────────────────────┐
│              PostgreSQL Database Engine                │
│     users, products, orders, cart_items, etc.          │
└────────────────────────────────────────────────────────┘
```

---

## 2. Component Interaction Flowchart

```mermaid
flowchart TD
    User([User at Desktop]) -->|Clicks Checkout| UI[CartPanel]
    UI -->|Async SwingWorker| Worker[AsyncOperation]
    Worker -->|Execute Background Thread| OS[OrderService]
    OS -->|1. Validate Buyer & Address| AD[AddressDao]
    OS -->|2. Open Dedicated Connection| DB[DatabaseConfig]
    OS -->|3. setAutoCommit(false)| Conn[JDBC Connection]
    OS -->|4. SELECT ... FOR UPDATE| PD[ProductDao]
    OS -->|5. Insert Order & Items| OD[OrderDao]
    OS -->|6. Deduct Stock| ID[InventoryDao]
    OS -->|7. Clear Cart & Add Tracking| CD[CartDao]
    OS -->|8. conn.commit()| Conn
    Conn -->|Commit OK| Worker
    Worker -->|Dispatch to EDT| UI
    UI -->|Show Success Dialog & Refresh| User
```

---

## 3. Concurrency & Thread Boundaries
- **Event Dispatch Thread (EDT):** Handles user interactions, window redraws, input fields, and modal popups. No blocking network/JDBC queries run here.
- **Worker Threads (ThreadPoolExecutor):** Managed by `BackgroundTaskManager`. Runs `AsyncOperation` tasks like SQL execution, table population, and inventory computations.
- **Critical Sections (ReentrantLocks):** Fine-grained per-product locks managed by `InventoryLockManager` coordinate thread safety during concurrent stock adjustments.
