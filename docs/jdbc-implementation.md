# JDBC Implementation & Database Operations (7 + 3 Marks Compliance)

This document describes the direct Java Database Connectivity (JDBC) architecture used for all desktop data operations, satisfying the criteria for:
- Classes for database operations (7 Marks)
- Database connectivity (JDBC) (3 Marks)

---

## 1. Direct JDBC Architecture (No ORM / Hibernate in Desktop Module)

Unlike the web backend which uses Spring Data JPA, the desktop module interacts with PostgreSQL strictly through standard Java JDBC APIs:
- `java.sql.DriverManager`
- `java.sql.Connection`
- `java.sql.PreparedStatement`
- `java.sql.ResultSet`
- `java.sql.SQLException`

---

## 2. Database Connection Factory (`DatabaseConfig`)

Connections are obtained dynamically using `DriverManager`:

```java
public static Connection getConnection() {
    try {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    } catch (SQLException e) {
        throw new DatabaseException("Failed to establish JDBC Connection: " + e.getMessage(), e);
    }
}
```

### Configuration Resolution:
1. Environment variables (`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`).
2. Fallback to JVM System Properties.
3. Fallback to local PostgreSQL defaults (`jdbc:postgresql://localhost:5432/shopsphere`, `shopsphere_user`).
4. Driver class: `org.postgresql.Driver` dynamically loaded via `Class.forName()`.

---

## 3. PreparedStatement & SQL Injection Prevention

SQL queries are strictly parameterized using `PreparedStatement`. No user input is ever concatenated into SQL strings.

Example from `JdbcProductDao`:
```java
String sql = "SELECT * FROM products WHERE UPPER(status) = 'ACTIVE' AND LOWER(name) LIKE ? ORDER BY id DESC";
try (Connection conn = getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.setString(1, "%" + keyword.toLowerCase() + "%");
    try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            results.add(mapResultSet(rs));
        }
    }
}
```

---

## 4. ResultSet Mapping to OOP Models

Every specialized JDBC DAO implements `mapResultSet(ResultSet rs)` to convert relational tuples into rich Java domain objects.

Example from `JdbcUserDao`:
```java
@Override
protected User mapResultSet(ResultSet rs) throws SQLException {
    Timestamp ts = rs.getTimestamp("created_at");
    return new User(
            rs.getLong("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getString("status"),
            rs.getBoolean("email_verified"),
            rs.getBoolean("phone_verified"),
            ts != null ? ts.toLocalDateTime() : null
    );
}
```

---

## 5. Comprehensive DAO Class Inventory

| Specialized DAO | Interface | Table Target | Key Responsibilities |
| :--- | :--- | :--- | :--- |
| `JdbcUserDao` | `UserDao` | `users` | Role-based lookups, BCrypt hash verification, status updates |
| `JdbcProductDao` | `ProductDao` | `products` | Catalog search, seller product CRUD, `SELECT FOR UPDATE` row locks |
| `JdbcCategoryDao` | `CategoryDao` | `categories` | Active category filters, unique name queries |
| `JdbcCartDao` | `CartDao` | `cart_items` | Quantity increments, subtotal calculations, transaction cart flush |
| `JdbcWishlistDao` | `WishlistDao` | `wishlist_items` | Existence checks, save/delete items |
| `JdbcAddressDao` | `AddressDao` | `addresses` | Buyer addresses, default address toggle, verification |
| `JdbcOrderDao` | `OrderDao` | `orders`, `order_items`, `order_tracking` | Transactional order insertion, batch item persistence, tracking |
| `JdbcInventoryDao` | `InventoryDao` | `products` | Atomic stock deductions, non-negative constraint checks |
| `JdbcActivityLogDao` | `ActivityLogDao` | `activity_logs` | Admin activity auditing |
