# Java Class Diagram (OOP Structure)

This document visualizes the class inheritance, interface implementation, and dependencies of the MARTplus desktop application.

---

## 1. Class Hierarchy Diagram (Mermaid)

```mermaid
classDiagram
    %% Interface Definitions
    class DashboardView {
        <<interface>>
        +initView() void
        +refreshData() void
        +getViewComponent() JComponent
        +getRoleName() String
        +onActivated() void
        +onDeactivated() void
    }

    class GenericDao~T, ID~ {
        <<interface>>
        +findById(ID id) Optional~T~
        +findAll() List~T~
        +save(T entity) T
        +update(T entity) void
        +deleteById(ID id) boolean
    }

    class UserDao {
        <<interface>>
        +findByEmailAndRole(String email, String role) Optional~User~
        +updateStatus(Long userId, String status) void
        +updateRole(Long userId, String role) void
    }

    class ProductDao {
        <<interface>>
        +findBySellerId(Long sellerId) List~Product~
        +searchByNameAndCategory(String kw, String cat) List~Product~
        +lockProductForUpdate(Connection conn, Long id) Optional~Product~
    }

    class OrderDao {
        <<interface>>
        +findByBuyerId(Long buyerId) List~Order~
        +insertOrder(Connection conn, Order order) Order
        +insertOrderItems(Connection conn, List~OrderItem~ items) void
    }

    %% Abstract Base Classes
    class BaseDashboard {
        <<abstract>>
        #User currentUser
        #MainWindow parentWindow
        #JPanel sidebarPanel
        #JPanel mainContentContainer
        #CardLayout contentCardLayout
        #buildNavigation(JPanel navPanel)* void
        +selectNavCard(String cardName) void
        +handleLogout() void
    }

    class AbstractJdbcDao~T, ID~ {
        <<abstract>>
        #getConnection() Connection
        #mapResultSet(ResultSet rs)* T
        #executeQueryForList(String sql, Object... params) List~T~
        #executeQueryForSingle(String sql, Object... params) Optional~T~
        #executeUpdate(String sql, Object... params) int
    }

    %% Concrete Implementations
    class BuyerDashboard {
        -ProductPanel productPanel
        -CartPanel cartPanel
        -OrderPanel orderPanel
        +initView() void
        +refreshData() void
        +buildNavigation(JPanel navPanel) void
    }

    class SellerDashboard {
        -ProductManagementPanel productPanel
        -InventoryPanel inventoryPanel
        -OrderPanel orderPanel
        +initView() void
        +refreshData() void
        +buildNavigation(JPanel navPanel) void
    }

    class AdminDashboard {
        -UserManagementPanel userPanel
        -ProductManagementPanel productPanel
        -OrderPanel orderPanel
        -ActivityLogPanel logPanel
        +initView() void
        +refreshData() void
        +buildNavigation(JPanel navPanel) void
    }

    class JdbcUserDao {
        #mapResultSet(ResultSet rs) User
    }

    class JdbcProductDao {
        #mapResultSet(ResultSet rs) Product
    }

    class JdbcOrderDao {
        #mapResultSet(ResultSet rs) Order
    }

    class RoleDashboardFactory {
        +createDashboard(User user, MainWindow window)$ DashboardView
    }

    %% Relationships
    DashboardView <|.. BaseDashboard : implements
    BaseDashboard <|-- BuyerDashboard : extends
    BaseDashboard <|-- SellerDashboard : extends
    BaseDashboard <|-- AdminDashboard : extends

    GenericDao <|.. AbstractJdbcDao : implements
    GenericDao <|-- UserDao : extends
    GenericDao <|-- ProductDao : extends
    GenericDao <|-- OrderDao : extends

    AbstractJdbcDao <|-- JdbcUserDao : extends
    AbstractJdbcDao <|-- JdbcProductDao : extends
    AbstractJdbcDao <|-- JdbcOrderDao : extends

    UserDao <|.. JdbcUserDao : implements
    ProductDao <|.. JdbcProductDao : implements
    OrderDao <|.. JdbcOrderDao : implements

    RoleDashboardFactory ..> DashboardView : creates
```

---

## 2. Key Design Patterns Demonstrated
1. **Factory Pattern:** `RoleDashboardFactory` encapsulates dynamic subclass selection and decouples `MainWindow` from concrete dashboard types.
2. **Template Method Pattern:** `BaseDashboard.buildNavigation(JPanel navPanel)` and `AbstractJdbcDao.mapResultSet(ResultSet rs)` define algorithm skeletons while allowing subclasses to provide custom implementations.
3. **Data Access Object (DAO) Pattern:** Encapsulates all SQL execution, connection handling, and ResultSet mapping behind typed contracts (`GenericDao<T, ID>`).
4. **Model-View-Controller (MVC) Pattern:** Strict decoupling between UI view panels, controllers, domain services, and DAOs.
