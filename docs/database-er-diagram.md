# Database Entity-Relationship (ER) Diagram

The desktop application directly reads and modifies the existing PostgreSQL schema shared with the MARTplus web application.

---

## 1. Entity-Relationship Diagram (Mermaid)

```mermaid
erDiagram
    USERS ||--o{ PRODUCTS : "sells"
    USERS ||--o{ CART_ITEMS : "places in cart"
    USERS ||--o{ WISHLIST_ITEMS : "wishes"
    USERS ||--o{ ADDRESSES : "owns"
    USERS ||--o{ ORDERS : "places"
    USERS ||--o{ ACTIVITY_LOGS : "performs (admin)"

    PRODUCTS ||--o{ CART_ITEMS : "referenced by"
    PRODUCTS ||--o{ WISHLIST_ITEMS : "referenced by"
    PRODUCTS ||--o{ ORDER_ITEMS : "included in"

    ORDERS ||--|{ ORDER_ITEMS : "contains"
    ORDERS ||--o| PAYMENTS : "paid via"
    ORDERS ||--o{ ORDER_TRACKING : "tracked by"
    ADDRESSES ||--o{ ORDERS : "delivers to"

    USERS {
        bigint id PK
        varchar first_name
        varchar last_name
        varchar email
        varchar phone
        varchar password
        varchar role
        varchar status
        boolean email_verified
        boolean phone_verified
        timestamp created_at
    }

    PRODUCTS {
        bigint id PK
        varchar name
        text description
        numeric price
        integer stock
        varchar category
        text image_url
        varchar status
        bigint seller_id FK
        timestamp created_at
    }

    CATEGORIES {
        bigint id PK
        varchar name UK
        text description
        varchar status
        timestamp created_at
    }

    CART_ITEMS {
        bigint id PK
        bigint buyer_id FK
        bigint product_id FK
        integer quantity
        timestamp created_at
        timestamp updated_at
    }

    WISHLIST_ITEMS {
        bigint id PK
        bigint buyer_id FK
        bigint product_id FK
        timestamp created_at
    }

    ADDRESSES {
        bigint id PK
        bigint buyer_id FK
        varchar full_name
        varchar phone
        text address_line
        varchar city
        varchar state
        varchar pincode
        boolean default_address
        timestamp created_at
        timestamp updated_at
    }

    ORDERS {
        bigint id PK
        bigint buyer_id FK
        bigint shipping_address_id FK
        numeric total_amount
        varchar status
        varchar payment_status
        varchar payment_method
        timestamp created_at
        timestamp updated_at
    }

    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint product_id FK
        integer quantity
        numeric price
        numeric subtotal
    }

    ORDER_TRACKING {
        bigint id PK
        bigint order_id FK
        varchar status
        text description
        timestamp created_at
    }

    PAYMENTS {
        bigint id PK
        bigint order_id FK
        numeric amount
        varchar method
        varchar status
        varchar transaction_id UK
        timestamp created_at
        timestamp updated_at
    }

    ACTIVITY_LOGS {
        bigint id PK
        bigint admin_id FK
        varchar action
        text description
        timestamp created_at
    }
```

---

## 2. Table & Column Mapping Table

| PostgreSQL Table | Java Model Class | Primary Key | Foreign Keys | Key Constraints |
| :--- | :--- | :--- | :--- | :--- |
| `users` | `User` | `id` (bigint) | None | Unique: `(email, role)`, `(phone, role)` |
| `products` | `Product` | `id` (bigint) | `seller_id` $\rightarrow$ `users(id)` | `stock >= 0`, `price > 0` |
| `categories` | `Category` | `id` (bigint) | None | Unique: `name` |
| `cart_items` | `CartItem` | `id` (bigint) | `buyer_id`, `product_id` | Unique: `(buyer_id, product_id)` |
| `wishlist_items` | `WishlistItem` | `id` (bigint) | `buyer_id`, `product_id` | Unique: `(buyer_id, product_id)` |
| `addresses` | `Address` | `id` (bigint) | `buyer_id` $\rightarrow$ `users(id)` | None |
| `orders` | `Order` | `id` (bigint) | `buyer_id`, `shipping_address_id` | None |
| `order_items` | `OrderItem` | `id` (bigint) | `order_id`, `product_id` | None |
| `order_tracking` | `OrderTracking`| `id` (bigint) | `order_id` $\rightarrow$ `orders(id)` | None |
| `payments` | `Payment` | `id` (bigint) | `order_id` $\rightarrow$ `orders(id)` | Unique: `order_id`, `transaction_id` |
| `activity_logs` | `ActivityLog` | `id` (bigint) | `admin_id` $\rightarrow$ `users(id)` | None |
