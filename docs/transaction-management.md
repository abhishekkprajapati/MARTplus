# JDBC Transaction Management (3 Marks Compliance)

This document provides a detailed breakdown of the multi-step atomic checkout transaction implemented in `OrderService.processCheckout()`.

---

## 1. Multi-Step Checkout Transaction Architecture

An e-commerce checkout is a critical distributed workflow that cannot tolerate partial state. If inventory is deducted but order insertion fails, or if an order is created without deducting inventory, financial and catalog inconsistency occurs.

The MARTplus desktop application coordinates an **atomic 12-step transaction** using raw JDBC primitives:
- `connection.setAutoCommit(false)`
- `connection.commit()`
- `connection.rollback()`

---

## 2. Step-by-Step Transaction Execution Flow

```mermaid
sequenceDiagram
    autonumber
    actor Buyer
    participant UI as CartPanel
    participant OS as OrderService
    participant Conn as JDBC Connection
    participant DB as PostgreSQL Database

    Buyer->>UI: Clicks "Place Order Now"
    UI->>OS: processCheckout(buyer, addressId, paymentMethod)
    OS->>Conn: DatabaseConfig.getConnection()
    OS->>Conn: setAutoCommit(false) [BEGIN TRANSACTION]

    loop For each CartItem
        OS->>DB: SELECT * FROM products WHERE id = ? FOR UPDATE
        DB-->>OS: Return locked product row
        OS->>OS: Verify status == ACTIVE and stock >= quantity
        OS->>OS: Accumulate total amount & prepare OrderItem
    end

    OS->>DB: INSERT INTO orders VALUES (...) RETURNING id
    DB-->>OS: Return generated order_id

    OS->>DB: INSERT INTO order_items (Batch Execution)
    OS->>DB: UPDATE products SET stock = stock - ? WHERE id = ?
    OS->>DB: DELETE FROM cart_items WHERE buyer_id = ?
    OS->>DB: INSERT INTO order_tracking VALUES (order_id, 'PLACED', ...)

    alt Success
        OS->>Conn: conn.commit()
        OS-->>UI: Return saved Order
        UI-->>Buyer: Show Success Dialog & Tracking Milestone
    else Any Failure / Exception (Stock Shortage or SQL Error)
        OS->>Conn: conn.rollback()
        OS-->>UI: Throw ValidationException / DatabaseException
        UI-->>Buyer: Show Error Dialog & Preserve Cart
    end

    OS->>Conn: setAutoCommit(true) & conn.close()
```

---

## 3. Transaction Safety & ACID Compliance

1. **Atomicity:** All 12 operations either complete together or none do. `conn.rollback()` ensures no dangling order rows or partial inventory deductions exist.
2. **Consistency:** Row-level checking prevents negative stock balances at both the Java application layer and the SQL constraint layer (`WHERE stock >= ?`).
3. **Isolation:** Rows are locked using `SELECT ... FOR UPDATE` within the active transaction, preventing other threads from modifying stock before this transaction commits.
4. **Durability:** Once `conn.commit()` returns, changes are persisted to the PostgreSQL write-ahead log (WAL).

---

## 4. Test Verification
- **`CheckoutTransactionTest.testCheckoutTransactionCommit`**:
  - Tests successful path: verifies `setAutoCommit(false)`, `insertOrder()`, `deductStock()`, `deleteByBuyerId()`, `insertTracking()`, and `conn.commit()`.
- **`CheckoutTransactionTest.testCheckoutTransactionRollbackOnStockShortage`**:
  - Tests failure path: simulates inventory shortfall during locked row check, verifies `conn.rollback()` is invoked and zero order items are saved.
