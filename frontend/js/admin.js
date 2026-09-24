/* =========================================================
   ShopSphere - Admin Shared JavaScript
   ========================================================= */

(function () {

    "use strict";

    const AUTH_KEY = "shopsphere_user";
    const USERS_KEY = "shopsphere_users";
    const PRODUCTS_KEY = "shopsphere_seller_products";
    const ORDERS_KEY = "shopsphere_orders";


    /* =====================================================
       STORAGE HELPERS
       ===================================================== */

    function getStorage(key, fallback) {
        try {
            const data = JSON.parse(
                localStorage.getItem(key)
            );

            return data === null || data === undefined
                ? fallback
                : data;

        } catch (error) {
            console.warn(
                "ShopSphere storage error:",
                key
            );

            return fallback;
        }
    }


    function setStorage(key, data) {
        try {
            localStorage.setItem(
                key,
                JSON.stringify(data)
            );

            return true;

        } catch (error) {
            console.error(
                "ShopSphere save error:",
                error
            );

            return false;
        }
    }


    /* =====================================================
       AUTH
       ===================================================== */

    function getCurrentUser() {

        const user = getStorage(
            AUTH_KEY,
            null
        );

        if (!user || typeof user !== "object") {
            return null;
        }

        return user;
    }


    function isLoggedIn() {

        const user =
            getCurrentUser();

        return !!(
            user &&
            user.loggedIn !== false
        );
    }


    function getAdminName() {

        const user =
            getCurrentUser();

        if (!user) {
            return "Administrator";
        }

        if (user.name) {
            return user.name;
        }

        const name = (
            (user.firstName || "") +
            " " +
            (user.lastName || "")
        ).trim();

        return name || "Administrator";
    }


    function getAdminInitial() {

        return getAdminName()
            .charAt(0)
            .toUpperCase();
    }


    function requireAdmin() {

        if (!isLoggedIn()) {

            window.location.href =
                "../login.html";

            return false;
        }


        const user =
            getCurrentUser();

        const role =
            String(
                user.role || "buyer"
            ).toLowerCase();


        if (role !== "admin") {

            if (
                window.ShopSphereAuth &&
                typeof window.ShopSphereAuth
                    .redirectToDashboard ===
                    "function"
            ) {

                window.ShopSphereAuth
                    .redirectToDashboard();

            } else {

                window.location.href =
                    "../login.html";
            }

            return false;
        }


        return true;
    }


    /* =====================================================
       USERS
       ===================================================== */

    function getUsers() {

        const users =
            getStorage(
                USERS_KEY,
                []
            );

        return Array.isArray(users)
            ? users
            : [];
    }


    function saveUsers(users) {

        return setStorage(
            USERS_KEY,
            users
        );
    }


    function getUserById(userId) {

        const users =
            getUsers();

        return (
            users.find(
                function (user) {

                    return String(
                        user.id
                    ) ===
                    String(userId);

                }
            ) ||
            null
        );
    }


    function addUser(userData) {

        const users =
            getUsers();


        const user = Object.assign(
            {
                id:
                    Date.now(),

                status:
                    "active",

                createdAt:
                    new Date().toISOString()
            },
            userData
        );


        if (!user.name) {

            user.name = (
                (user.firstName || "") +
                " " +
                (user.lastName || "")
            ).trim();

        }


        users.push(user);

        saveUsers(users);

        return user;
    }


    function updateUser(userId, updates) {

        const users =
            getUsers();

        const index =
            users.findIndex(
                function (user) {

                    return String(
                        user.id
                    ) ===
                    String(userId);

                }
            );


        if (index === -1) {
            return null;
        }


        users[index] =
            Object.assign(
                {},
                users[index],
                updates,
                {
                    updatedAt:
                        new Date().toISOString()
                }
            );


        if (
            !users[index].name ||
            updates.firstName !== undefined ||
            updates.lastName !== undefined
        ) {

            users[index].name = (
                (users[index].firstName || "") +
                " " +
                (users[index].lastName || "")
            ).trim();

        }


        saveUsers(users);

        return users[index];
    }


    function deleteUser(userId) {

        const users =
            getUsers();

        const filtered =
            users.filter(
                function (user) {

                    return String(
                        user.id
                    ) !==
                    String(userId);

                }
            );


        if (
            filtered.length ===
            users.length
        ) {
            return false;
        }


        saveUsers(filtered);

        if (window.ShopSphereApi && window.ShopSphereAuth && window.ShopSphereAuth.isAdmin()) {
            window.ShopSphereApi.admin.deactivateUser(userId).catch(function (err) {
                console.warn("Backend user deactivation notice:", err);
            });
        }

        return true;
    }

    function deactivateUser(userId) {
        return deleteUser(userId);
    }


    function getUserStats() {

        const users =
            getUsers();


        return {

            total:
                users.length,

            buyers:
                users.filter(
                    function (user) {

                        return String(
                            user.role || "buyer"
                        ).toLowerCase() ===
                        "buyer";

                    }
                ).length,

            sellers:
                users.filter(
                    function (user) {

                        return String(
                            user.role || ""
                        ).toLowerCase() ===
                        "seller";

                    }
                ).length,

            admins:
                users.filter(
                    function (user) {

                        return String(
                            user.role || ""
                        ).toLowerCase() ===
                        "admin";

                    }
                ).length,

            active:
                users.filter(
                    function (user) {

                        return String(
                            user.status || "active"
                        ).toLowerCase() ===
                        "active";

                    }
                ).length

        };
    }


    /* =====================================================
       PRODUCTS
       ===================================================== */

    function getSellerProducts() {

        const products =
            getStorage(
                PRODUCTS_KEY,
                []
            );

        return Array.isArray(products)
            ? products
            : [];
    }


    function getMarketplaceProducts() {

        if (
            window.ShopSphereProducts &&
            Array.isArray(
                window.ShopSphereProducts.products
            )
        ) {

            return window.ShopSphereProducts.products;
        }

        return [];
    }


    function getProducts() {

        const marketplace =
            getMarketplaceProducts();

        const sellerProducts =
            getSellerProducts();


        if (!marketplace.length) {
            return sellerProducts;
        }


        if (!sellerProducts.length) {
            return marketplace;
        }


        const products =
            marketplace.slice();


        sellerProducts.forEach(
            function (sellerProduct) {

                const index =
                    products.findIndex(
                        function (product) {

                            return Number(
                                product.id
                            ) === Number(
                                sellerProduct.id
                            );

                        }
                    );


                if (index >= 0) {

                    products[index] =
                        Object.assign(
                            {},
                            products[index],
                            sellerProduct
                        );

                } else {

                    products.push(
                        sellerProduct
                    );
                }

            }
        );


        return products;
    }


    function getProductById(productId) {

        const id =
            Number(productId);

        return (
            getProducts().find(
                function (product) {

                    return Number(
                        product.id
                    ) === id;

                }
            ) ||
            null
        );
    }


    function updateProduct(productId, updates) {

        const id =
            Number(productId);

        const products =
            getSellerProducts();


        const index =
            products.findIndex(
                function (product) {

                    return Number(
                        product.id
                    ) === id;

                }
            );


        if (index >= 0) {

            products[index] =
                Object.assign(
                    {},
                    products[index],
                    updates,
                    {
                        updatedAt:
                            new Date().toISOString()
                    }
                );


            setStorage(
                PRODUCTS_KEY,
                products
            );
        }


        /* Update shared marketplace data */

        if (
            window.ShopSphereProducts &&
            Array.isArray(
                window.ShopSphereProducts.products
            )
        ) {

            const sharedIndex =
                window.ShopSphereProducts.products
                    .findIndex(
                        function (product) {

                            return Number(
                                product.id
                            ) === id;

                        }
                    );


            if (sharedIndex >= 0) {

                window.ShopSphereProducts.products[
                    sharedIndex
                ] =
                    Object.assign(
                        {},
                        window.ShopSphereProducts.products[
                            sharedIndex
                        ],
                        updates
                    );
            }
        }


        return getProductById(id);
    }


    function deleteProduct(productId) {

        const id =
            Number(productId);

        let products =
            getSellerProducts();


        const originalLength =
            products.length;


        products =
            products.filter(
                function (product) {

                    return Number(
                        product.id
                    ) !== id;

                }
            );


        if (
            products.length ===
            originalLength
        ) {

            /*
             * If the product belongs only to
             * the temporary marketplace array,
             * remove it there as well.
             */
            if (
                window.ShopSphereProducts &&
                Array.isArray(
                    window.ShopSphereProducts.products
                )
            ) {

                const before =
                    window.ShopSphereProducts.products
                        .length;

                window.ShopSphereProducts.products =
                    window.ShopSphereProducts.products
                        .filter(
                            function (product) {

                                return Number(
                                    product.id
                                ) !== id;

                            }
                        );

                return (
                    window.ShopSphereProducts.products
                        .length < before
                );
            }

            return false;
        }


        setStorage(
            PRODUCTS_KEY,
            products
        );


        if (
            window.ShopSphereProducts &&
            Array.isArray(
                window.ShopSphereProducts.products
            )
        ) {

            window.ShopSphereProducts.products =
                window.ShopSphereProducts.products
                    .filter(
                        function (product) {

                            return Number(
                                product.id
                            ) !== id;

                        }
                    );
        }

        if (window.ShopSphereApi && window.ShopSphereAuth && window.ShopSphereAuth.isAdmin()) {
            window.ShopSphereApi.admin.deleteProduct(id).catch(function (err) {
                console.warn("Backend product deletion notice:", err);
            });
        }

        return true;
    }


    function getProductStats() {

        const products =
            getProducts();


        return {

            total:
                products.length,

            active:
                products.filter(
                    function (product) {

                        return String(
                            product.status || "active"
                        ).toLowerCase() ===
                        "active";

                    }
                ).length,

            lowStock:
                products.filter(
                    function (product) {

                        const stock =
                            Number(
                                product.stock || 0
                            );

                        return stock > 0 &&
                               stock <= 10;

                    }
                ).length,

            outOfStock:
                products.filter(
                    function (product) {

                        return Number(
                            product.stock || 0
                        ) <= 0;

                    }
                ).length

        };
    }


    /* =====================================================
       ORDERS
       ===================================================== */

    function getOrders() {

        const orders =
            getStorage(
                ORDERS_KEY,
                []
            );

        return Array.isArray(orders)
            ? orders
            : [];
    }


    function saveOrders(orders) {

        return setStorage(
            ORDERS_KEY,
            orders
        );
    }


    function getOrderId(order) {

        return String(
            order?.orderId ??
            order?.id ??
            ""
        );
    }


    function getOrderById(orderId) {

        return (
            getOrders().find(
                function (order) {

                    return getOrderId(
                        order
                    ) ===
                    String(orderId);

                }
            ) ||
            null
        );
    }


    function updateOrderStatus(
        orderId,
        status
    ) {

        const orders =
            getOrders();

        const index =
            orders.findIndex(
                function (order) {

                    return getOrderId(
                        order
                    ) ===
                    String(orderId);

                }
            );


        if (index === -1) {
            return null;
        }


        orders[index].status =
            status;

        orders[index].updatedAt =
            new Date().toISOString();


        saveOrders(orders);

        if (window.ShopSphereApi && window.ShopSphereAuth && window.ShopSphereAuth.isAdmin()) {
            const numericId = String(orderId).replace(/\D/g, "") || orderId;
            window.ShopSphereApi.admin.updateOrderStatus(numericId, status).catch(function (err) {
                console.warn("Backend order status update notice:", err);
            });
        }

        return orders[index];
    }


    function getOrderTotal(order) {

        return Number(
            order?.total ??
            order?.grandTotal ??
            0
        );
    }


    function getOrderStats() {

        const orders =
            getOrders();


        return {

            total:
                orders.length,

            processing:
                orders.filter(
                    function (order) {

                        return String(
                            order.status ||
                            "Processing"
                        ).toLowerCase() ===
                        "processing";

                    }
                ).length,

            shipped:
                orders.filter(
                    function (order) {

                        return String(
                            order.status || ""
                        ).toLowerCase() ===
                        "shipped";

                    }
                ).length,

            outForDelivery:
                orders.filter(
                    function (order) {

                        return String(
                            order.status || ""
                        ).toLowerCase() ===
                        "out for delivery";

                    }
                ).length,

            delivered:
                orders.filter(
                    function (order) {

                        return String(
                            order.status || ""
                        ).toLowerCase() ===
                        "delivered";

                    }
                ).length,

            cancelled:
                orders.filter(
                    function (order) {

                        return String(
                            order.status || ""
                        ).toLowerCase() ===
                        "cancelled";

                    }
                ).length,

            revenue:
                orders.reduce(
                    function (total, order) {

                        return total +
                            getOrderTotal(
                                order
                            );

                    },
                    0
                )

        };
    }


    /* =====================================================
       DASHBOARD SUMMARY
       ===================================================== */

    let backendDashboardData = null;

    function getDashboardStats() {

        if (backendDashboardData) {
            return {
                users: Number(backendDashboardData.totalUsers) || 0,
                products: Number(backendDashboardData.totalProducts) || 0,
                orders: Number(backendDashboardData.totalOrders) || 0,
                revenue: Number(backendDashboardData.totalRevenue) || 0,
                activeUsers: Number(backendDashboardData.totalUsers) || 0,
                activeProducts: Number(backendDashboardData.totalProducts) || 0,
                processingOrders: Number(backendDashboardData.pendingOrders) || 0,
                deliveredOrders: Number(backendDashboardData.completedOrders) || 0
            };
        }

        const userStats =
            getUserStats();

        const productStats =
            getProductStats();

        const orderStats =
            getOrderStats();


        return {

            users:
                userStats.total,

            products:
                productStats.total,

            orders:
                orderStats.total,

            revenue:
                orderStats.revenue,

            activeUsers:
                userStats.active,

            activeProducts:
                productStats.active,

            processingOrders:
                orderStats.processing,

            deliveredOrders:
                orderStats.delivered

        };
    }


    /* =====================================================
       ACTIVITY
       ===================================================== */

    function createActivity(
        type,
        title,
        description,
        user
    ) {

        const activity = {

            id:
                Date.now() +
                Math.floor(
                    Math.random() * 1000
                ),

            type:
                type || "system",

            title:
                title || "System activity",

            description:
                description || "",

            user:
                user || getAdminName(),

            createdAt:
                new Date().toISOString()

        };


        const key =
            "shopsphere_admin_activity";

        const activities =
            getStorage(
                key,
                []
            );


        const list =
            Array.isArray(activities)
                ? activities
                : [];


        list.unshift(
            activity
        );


        setStorage(
            key,
            list.slice(0, 100)
        );


        return activity;
    }


    function getActivities() {

        const activities =
            getStorage(
                "shopsphere_admin_activity",
                []
            );

        return Array.isArray(activities)
            ? activities
            : [];
    }


    /* =====================================================
       FORMAT HELPERS
       ===================================================== */

    function formatPrice(value) {

        return "₹" +
            Number(
                value || 0
            ).toLocaleString(
                "en-IN"
            );
    }


    function formatDate(value) {

        if (!value) {
            return "N/A";
        }


        const date =
            new Date(value);


        if (
            Number.isNaN(
                date.getTime()
            )
        ) {
            return "N/A";
        }


        return date.toLocaleDateString(
            "en-IN",
            {
                day: "2-digit",
                month: "short",
                year: "numeric"
            }
        );
    }


    function getStatusClass(status) {

        const value =
            String(
                status || ""
            ).toLowerCase();


        if (
            value === "delivered" ||
            value === "active" ||
            value === "completed"
        ) {
            return "success";
        }


        if (
            value === "cancelled" ||
            value === "inactive" ||
            value === "failed"
        ) {
            return "danger";
        }


        if (
            value === "shipped" ||
            value === "out for delivery"
        ) {
            return "info";
        }


        return "warning";
    }


    /* =====================================================
       TOAST
       ===================================================== */

    function showToast(message) {

        if (
            window.ShopSphere &&
            typeof window.ShopSphere.showToast ===
                "function"
        ) {

            window.ShopSphere.showToast(
                message
            );

            return;
        }


        let toast =
            document.getElementById(
                "toast"
            );


        if (!toast) {

            toast =
                document.createElement(
                    "div"
                );

            toast.id = "toast";
            toast.className = "toast";

            document.body.appendChild(
                toast
            );
        }


        toast.textContent =
            message;

        toast.classList.add(
            "show"
        );


        setTimeout(
            function () {

                toast.classList.remove(
                    "show"
                );

            },
            2200
        );
    }


    /* =====================================================
       ADMIN UI
       ===================================================== */

    function updateAdminUI() {

        const name =
            getAdminName();

        const initial =
            getAdminInitial();


        document
            .querySelectorAll(
                "[data-admin-name]"
            )
            .forEach(
                function (element) {

                    element.textContent =
                        name;

                }
            );


        document
            .querySelectorAll(
                "[data-admin-initial]"
            )
            .forEach(
                function (element) {

                    element.textContent =
                        initial;

                }
            );
    }


    /* =====================================================
       LOGOUT
       ===================================================== */

    function logout() {

        if (
            window.ShopSphereAuth &&
            typeof window.ShopSphereAuth.logout ===
                "function"
        ) {

            window.ShopSphereAuth.logout();

            return;
        }


        const user =
            getCurrentUser();


        if (user) {

            user.loggedIn =
                false;

            setStorage(
                AUTH_KEY,
                user
            );
        }


        window.location.href =
            "../login.html";
    }


    /* =====================================================
       BACKEND SYNC
       ===================================================== */

    async function syncAdminFromBackend() {
        if (!window.ShopSphereAuth || !window.ShopSphereAuth.isAdmin()) return;
        const api = window.ShopSphereApi;
        if (!api) return;

        try {
            // 1. Dashboard summary
            const dash = await api.admin.getDashboard().catch(() => null);
            if (dash) {
                backendDashboardData = dash;
            }

            // 2. Live Users
            const users = await api.admin.getUsers().catch(() => null);
            if (Array.isArray(users)) {
                const mappedUsers = users.map(function (u) {
                    return {
                        id: u.id,
                        firstName: u.firstName,
                        lastName: u.lastName,
                        name: (u.firstName || "") + (u.lastName ? " " + u.lastName : ""),
                        email: u.email,
                        phone: u.phone || "",
                        role: (u.role || "BUYER").toLowerCase(),
                        status: (u.status || "ACTIVE").toLowerCase(),
                        createdAt: u.createdAt
                    };
                });
                setStorage(USERS_KEY, mappedUsers);
            }

            // 3. Live Products
            const prods = await api.admin.getProducts().catch(() => null);
            if (Array.isArray(prods)) {
                const mappedProds = prods.map(function (p) {
                    return {
                        id: p.id,
                        name: p.name,
                        description: p.description,
                        category: p.category,
                        price: Number(p.price),
                        stock: p.stock ?? 0,
                        imageUrl: p.imageUrl || "",
                        status: (p.status || "ACTIVE").toLowerCase(),
                        seller: p.seller ? (p.seller.firstName + (p.seller.lastName ? " " + p.seller.lastName : "")) : "Seller",
                        sellerEmail: p.seller?.email || "",
                        createdAt: p.createdAt
                    };
                });
                setStorage(PRODUCTS_KEY, mappedProds);
            }

            // 4. Live Orders
            const orders = await api.admin.getOrders().catch(() => null);
            if (Array.isArray(orders)) {
                const mappedOrders = orders.map(function (o) {
                    return {
                        id: o.id,
                        orderId: "ORD-" + String(o.id).padStart(6, "0"),
                        orderNumber: "ORD-" + String(o.id).padStart(6, "0"),
                        customerName: o.buyerName || o.customerName || "Customer",
                        name: o.buyerName || o.customerName || "Customer",
                        buyerEmail: o.buyerEmail || "",
                        customerEmail: o.buyerEmail || "",
                        date: o.createdAt,
                        createdAt: o.createdAt,
                        status: o.status,
                        total: Number(o.totalAmount) || 0,
                        amount: Number(o.totalAmount) || 0,
                        grandTotal: Number(o.totalAmount) || 0,
                        paymentMethod: o.paymentMethod || "Card",
                        deliveryAddress: o.deliveryAddress || (o.address ? (o.address.street + ", " + o.address.city) : "Standard Delivery"),
                        address: o.deliveryAddress || (o.address ? (o.address.street + ", " + o.address.city) : "Standard Delivery"),
                        items: (o.items || []).map(function (i) {
                            return {
                                id: i.productId,
                                name: i.productName || "Product",
                                productName: i.productName || "Product",
                                quantity: i.quantity,
                                price: Number(i.price),
                                unitPrice: Number(i.price),
                                subtotal: Number(i.subtotal)
                            };
                        })
                    };
                });
                setStorage(ORDERS_KEY, mappedOrders);
            }

            // 5. Live Activity Logs
            const logs = await api.admin.getAllActivity().catch(() => null);
            if (Array.isArray(logs)) {
                const mappedLogs = logs.map(function (l) {
                    let type = "system";
                    const act = (l.action || "").toUpperCase();
                    if (act.startsWith("USER_")) type = "user";
                    else if (act.startsWith("PRODUCT_")) type = "product";
                    else if (act.startsWith("ORDER_")) type = "order";
                    else if (act.startsWith("AUTH_")) type = "security";

                    return {
                        id: l.id,
                        type: type,
                        action: l.action,
                        title: (l.action || "Activity").replace(/_/g, " ").replace(/\b\w/g, function (c) { return c.toUpperCase(); }),
                        description: l.description || "",
                        user: l.adminEmail || "Admin",
                        createdAt: l.createdAt,
                        date: l.createdAt
                    };
                });
                setStorage("shopsphere_admin_activity", mappedLogs);
            }

            updateAdminUI();

            // Trigger UI callbacks if registered on page
            if (typeof window.renderStats === "function") window.renderStats();
            if (typeof window.renderUsers === "function") window.renderUsers();
            if (typeof window.renderProducts === "function") window.renderProducts();
            if (typeof window.renderOrders === "function") window.renderOrders();
            if (typeof window.renderActivities === "function") window.renderActivities();
            if (typeof window.buildActivities === "function") window.buildActivities();
            if (typeof window.updateDashboardStats === "function") window.updateDashboardStats();

        } catch (err) {
            console.warn("Backend admin sync notice:", err);
        }
    }

    /* =====================================================
       INITIALIZE
       ===================================================== */

    function initialize() {

        updateAdminUI();

        syncAdminFromBackend();


        document
            .querySelectorAll(
                "[data-admin-logout]"
            )
            .forEach(
                function (button) {

                    button.addEventListener(
                        "click",
                        function (event) {

                            event.preventDefault();

                            logout();

                        }
                    );

                }
            );
    }


    /* =====================================================
       PUBLIC API
       ===================================================== */

    window.ShopSphereAdmin = {

        getCurrentUser:
            getCurrentUser,

        isLoggedIn:
            isLoggedIn,

        getAdminName:
            getAdminName,

        getAdminInitial:
            getAdminInitial,

        requireAdmin:
            requireAdmin,

        getUsers:
            getUsers,

        saveUsers:
            saveUsers,

        getUserById:
            getUserById,

        addUser:
            addUser,

        updateUser:
            updateUser,

        deleteUser:
            deleteUser,

        deactivateUser:
            deactivateUser,

        getUserStats:
            getUserStats,

        getProducts:
            getProducts,

        getProductById:
            getProductById,

        updateProduct:
            updateProduct,

        deleteProduct:
            deleteProduct,

        getProductStats:
            getProductStats,

        getOrders:
            getOrders,

        saveOrders:
            saveOrders,

        getOrderById:
            getOrderById,

        updateOrderStatus:
            updateOrderStatus,

        getOrderTotal:
            getOrderTotal,

        getOrderStats:
            getOrderStats,

        getDashboardStats:
            getDashboardStats,

        createActivity:
            createActivity,

        getActivities:
            getActivities,

        formatPrice:
            formatPrice,

        formatDate:
            formatDate,

        getStatusClass:
            getStatusClass,

        showToast:
            showToast,

        updateAdminUI:
            updateAdminUI,

        logout:
            logout,

        getStorage:
            getStorage,

        setStorage:
            setStorage,

        syncAdminFromBackend:
            syncAdminFromBackend
    };


    document.addEventListener(
        "DOMContentLoaded",
        initialize
    );

})();