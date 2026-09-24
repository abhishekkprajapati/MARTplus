/* =========================================================
   ShopSphere - Seller Shared JavaScript
   ========================================================= */

(function () {

    "use strict";


    const AUTH_KEY = "shopsphere_user";
    const SELLER_PRODUCTS_KEY = "shopsphere_seller_products";
    const ORDERS_KEY = "shopsphere_orders";


    /* =====================================================
       STORAGE
       ===================================================== */

    function getStorage(key, fallback) {

        try {

            const data =
                JSON.parse(
                    localStorage.getItem(key)
                );

            return data === null ||
                   data === undefined
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

        const user =
            getStorage(
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


    function getSellerName() {

        const user =
            getCurrentUser();

        if (!user) {
            return "Seller";
        }

        if (user.name) {
            return user.name;
        }

        const name = (
            (user.firstName || "") +
            " " +
            (user.lastName || "")
        ).trim();

        return name || "Seller";
    }


    function getSellerInitial() {

        return getSellerName()
            .charAt(0)
            .toUpperCase();
    }


    function getSellerEmail() {

        const user =
            getCurrentUser();

        return user?.email || "";
    }


    function requireSeller() {

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


        if (role !== "seller") {

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
       PRODUCTS
       ===================================================== */

    function getSellerProducts() {

        const products =
            getStorage(
                SELLER_PRODUCTS_KEY,
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


    function getAllProducts() {

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


        const result =
            marketplace.slice();


        sellerProducts.forEach(
            function (sellerProduct) {

                const existingIndex =
                    result.findIndex(
                        function (product) {

                            return Number(
                                product.id
                            ) === Number(
                                sellerProduct.id
                            );

                        }
                    );


                if (existingIndex >= 0) {

                    result[existingIndex] =
                        Object.assign(
                            {},
                            result[existingIndex],
                            sellerProduct
                        );

                } else {

                    result.push(
                        sellerProduct
                    );
                }

            }
        );


        return result;
    }


    function saveSellerProducts(products) {

        return setStorage(
            SELLER_PRODUCTS_KEY,
            products
        );
    }


    function getProductById(productId) {

        const id =
            Number(productId);

        const products =
            getAllProducts();

        return (
            products.find(
                function (product) {

                    return Number(
                        product.id
                    ) === id;

                }
            ) ||
            null
        );
    }


    async function addProduct(product) {

        let backendCreated = null;
        if (window.ShopSphereAuth && window.ShopSphereAuth.isSeller() && window.ShopSphereApi) {
            try {
                backendCreated = await window.ShopSphereApi.products.create({
                    name: product.name,
                    description: product.description || "",
                    price: Number(product.price),
                    stock: Number(product.stock) || 0,
                    category: product.category || "General",
                    imageUrl: product.imageUrl || product.image || ""
                });
            } catch (err) {
                console.error("Backend addProduct error:", err);
                throw err;
            }
        }

        const products =
            getSellerProducts();

        const newProduct =
            Object.assign(
                {
                    id: backendCreated ? backendCreated.id : Date.now(),
                    createdAt: backendCreated?.createdAt || new Date().toISOString(),
                    status: "active"
                },
                product
            );

        products.push(
            newProduct
        );

        saveSellerProducts(
            products
        );

        /* Also update shared marketplace list */
        if (
            window.ShopSphereProducts &&
            Array.isArray(
                window.ShopSphereProducts.products
            )
        ) {
            window.ShopSphereProducts.products.push(
                newProduct
            );
        }

        return newProduct;
    }


    async function updateProduct(productId, updates) {

        const id =
            Number(productId);

        if (window.ShopSphereAuth && window.ShopSphereAuth.isSeller() && window.ShopSphereApi) {
            try {
                await window.ShopSphereApi.products.update(id, {
                    name: updates.name,
                    description: updates.description,
                    price: Number(updates.price),
                    stock: Number(updates.stock),
                    category: updates.category,
                    imageUrl: updates.imageUrl || updates.image
                });
            } catch (err) {
                console.warn("Backend updateProduct notice:", err);
            }
        }

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

        if (index === -1) {
            return null;
        }

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

        saveSellerProducts(
            products
        );

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

        return products[index];
    }


    async function deleteProduct(productId) {

        const id =
            Number(productId);

        if (window.ShopSphereAuth && window.ShopSphereAuth.isSeller() && window.ShopSphereApi) {
            try {
                await window.ShopSphereApi.products.delete(id);
            } catch (err) {
                console.warn("Backend deleteProduct notice:", err);
            }
        }

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

            return false;
        }


        saveSellerProducts(
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


        return true;
    }


    /* =====================================================
       INVENTORY
       ===================================================== */

    async function updateStock(productId, newStock) {
        const id = Number(productId);
        const qty = Math.max(0, parseInt(newStock, 10) || 0);

        const products = getSellerProducts();
        const prod = products.find(p => Number(p.id) === id);
        if (prod) {
            prod.stock = qty;
            saveSellerProducts(products);
        }

        if (window.ShopSphereApi && window.ShopSphereAuth && window.ShopSphereAuth.isSeller()) {
            try {
                await window.ShopSphereApi.products.updateStock(id, qty);
            } catch (err) {
                console.warn("Backend stock update error:", err);
            }
        }
        return true;
    }

    function getStock(product) {

        return Math.max(
            0,
            Number(
                product?.stock || 0
            )
        );
    }


    function getStockStatus(stock) {

        const quantity =
            Number(stock || 0);


        if (quantity <= 0) {
            return "out";
        }

        if (quantity <= 10) {
            return "low";
        }

        return "healthy";
    }


    function getStockLabel(stock) {

        const status =
            getStockStatus(stock);

        if (status === "out") {
            return "Out of Stock";
        }

        if (status === "low") {
            return "Low Stock";
        }

        return "In Stock";
    }


    function getInventoryStats() {

        const products =
            getAllProducts();


        let totalStock = 0;
        let lowStock = 0;
        let outOfStock = 0;


        products.forEach(
            function (product) {

                const stock =
                    getStock(product);

                totalStock +=
                    stock;


                if (stock <= 0) {
                    outOfStock++;
                } else if (stock <= 10) {
                    lowStock++;
                }

            }
        );


        return {

            totalProducts:
                products.length,

            totalStock:
                totalStock,

            lowStock:
                lowStock,

            outOfStock:
                outOfStock

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


    function getOrderStatus(order) {

        return String(
            order?.status ||
            "Processing"
        );
    }


    function getOrderTotal(order) {

        return Number(
            order?.total ??
            order?.grandTotal ??
            0
        );
    }


    function getOrderItems(order) {

        if (
            Array.isArray(
                order?.items
            )
        ) {

            return order.items;
        }

        if (
            Array.isArray(
                order?.products
            )
        ) {

            return order.products;
        }

        return [];
    }


    function getProductsSold() {

        return getOrders()
            .reduce(
                function (total, order) {

                    const items =
                        getOrderItems(
                            order
                        );


                    return total +
                        items.reduce(
                            function (
                                itemTotal,
                                item
                            ) {

                                return itemTotal +
                                    Number(
                                        item.quantity ||
                                        1
                                    );

                            },
                            0
                        );

                },
                0
            );
    }


    function getSalesStats() {

        const orders =
            getOrders();


        const revenue =
            orders.reduce(
                function (total, order) {

                    return total +
                        getOrderTotal(
                            order
                        );

                },
                0
            );


        const orderCount =
            orders.length;


        const productsSold =
            getProductsSold();


        const averageOrder =
            orderCount
                ? revenue / orderCount
                : 0;


        return {

            revenue:
                revenue,

            orders:
                orderCount,

            productsSold:
                productsSold,

            averageOrder:
                averageOrder

        };
    }


    function getOrdersByStatus() {

        const orders =
            getOrders();


        return {

            processing:
                orders.filter(
                    function (order) {

                        return getOrderStatus(
                            order
                        ).toLowerCase() ===
                            "processing";

                    }
                ).length,

            shipped:
                orders.filter(
                    function (order) {

                        return getOrderStatus(
                            order
                        ).toLowerCase() ===
                            "shipped";

                    }
                ).length,

            outForDelivery:
                orders.filter(
                    function (order) {

                        return getOrderStatus(
                            order
                        ).toLowerCase() ===
                            "out for delivery";

                    }
                ).length,

            delivered:
                orders.filter(
                    function (order) {

                        return getOrderStatus(
                            order
                        ).toLowerCase() ===
                            "delivered";

                    }
                ).length,

            cancelled:
                orders.filter(
                    function (order) {

                        return getOrderStatus(
                            order
                        ).toLowerCase() ===
                            "cancelled";

                    }
                ).length

        };
    }


    function updateOrderStatus(
        orderId,
        newStatus
    ) {

        const orders =
            getOrders();


        const index =
            orders.findIndex(
                function (order) {

                    return String(
                        order.orderId ??
                        order.id
                    ) ===
                    String(orderId);

                }
            );


        if (index === -1) {
            return false;
        }


        orders[index].status =
            newStatus;

        orders[index].updatedAt =
            new Date().toISOString();


        setStorage(
            ORDERS_KEY,
            orders
        );


        return true;
    }


    /* =====================================================
       ANALYTICS
       ===================================================== */

    function getCategorySales() {

        const orders =
            getOrders();

        const categorySales = {};


        orders.forEach(
            function (order) {

                const items =
                    getOrderItems(
                        order
                    );


                items.forEach(
                    function (item) {

                        const product =
                            getProductById(
                                item.productId ??
                                item.id
                            );


                        const category =
                            product?.category ||
                            item.category ||
                            "Other";


                        const quantity =
                            Number(
                                item.quantity ||
                                1
                            );


                        const price =
                            Number(
                                item.price ||
                                product?.price ||
                                0
                            );


                        if (
                            !categorySales[
                                category
                            ]
                        ) {

                            categorySales[
                                category
                            ] = {
                                quantity: 0,
                                revenue: 0
                            };

                        }


                        categorySales[
                            category
                        ].quantity +=
                            quantity;


                        categorySales[
                            category
                        ].revenue +=
                            quantity *
                            price;

                    }
                );

            }
        );


        return categorySales;
    }


    function getTopProducts() {

        const orders =
            getOrders();

        const productMap = {};


        orders.forEach(
            function (order) {

                getOrderItems(
                    order
                ).forEach(
                    function (item) {

                        const id =
                            Number(
                                item.productId ??
                                item.id
                            );


                        if (!Number.isFinite(id)) {
                            return;
                        }


                        const product =
                            getProductById(id);


                        const name =
                            product?.name ||
                            item.name ||
                            "Product";


                        const quantity =
                            Number(
                                item.quantity ||
                                1
                            );


                        const revenue =
                            quantity *
                            Number(
                                item.price ||
                                product?.price ||
                                0
                            );


                        if (!productMap[id]) {

                            productMap[id] = {

                                id: id,

                                name: name,

                                quantity: 0,

                                revenue: 0

                            };

                        }


                        productMap[id].quantity +=
                            quantity;

                        productMap[id].revenue +=
                            revenue;

                    }
                );

            }
        );


        return Object.values(
            productMap
        ).sort(
            function (a, b) {

                return b.quantity -
                    a.quantity;

            }
        );
    }


    /* =====================================================
       FORMATTING
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
       SELLER UI
       ===================================================== */

    function updateSellerUI() {

        const name =
            getSellerName();

        const initial =
            getSellerInitial();

        const email =
            getSellerEmail();


        document
            .querySelectorAll(
                "[data-seller-name]"
            )
            .forEach(
                function (element) {

                    element.textContent =
                        name;

                }
            );


        document
            .querySelectorAll(
                "[data-seller-email]"
            )
            .forEach(
                function (element) {

                    element.textContent =
                        email;

                }
            );


        document
            .querySelectorAll(
                "[data-seller-initial]"
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
       EVENT BINDINGS
       ===================================================== */

    function bindSellerEvents() {

        document
            .querySelectorAll(
                "[data-seller-logout]"
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
       BACKEND SYNC
       ===================================================== */

    async function syncSellerFromBackend() {
        if (!window.ShopSphereAuth || !window.ShopSphereAuth.isSeller()) return;
        const api = window.ShopSphereApi;
        if (!api) return;

        try {
            // 1. My Products
            const myProducts = await api.products.getMyProducts().catch(() => null);
            if (Array.isArray(myProducts)) {
                const mapped = myProducts.map(p => ({
                    id: p.id,
                    name: p.name,
                    description: p.description,
                    category: p.category,
                    price: Number(p.price),
                    stock: p.stock ?? 0,
                    imageUrl: p.imageUrl || "",
                    status: (p.status || "ACTIVE").toLowerCase(),
                    createdAt: p.createdAt
                }));
                setStorage(SELLER_PRODUCTS_KEY, mapped);
            }

            // 2. Seller Orders
            const sellerOrders = await api.seller.getOrders().catch(() => null);
            if (Array.isArray(sellerOrders)) {
                const grouped = {};
                sellerOrders.forEach(so => {
                    const oId = so.orderId || so.id;
                    if (!grouped[oId]) {
                        grouped[oId] = {
                            id: oId,
                            orderId: oId,
                            orderNumber: "SS" + String(oId).padStart(6, "0"),
                            date: so.orderCreatedAt || so.createdAt,
                            createdAt: so.orderCreatedAt || so.createdAt,
                            status: so.orderStatus || so.status || "Processing",
                            payment: so.paymentMethod || "Card",
                            paymentStatus: so.paymentStatus || "PAID",
                            buyerName: so.buyerName || "",
                            buyerEmail: so.buyerEmail || "",
                            total: 0,
                            items: []
                        };
                    }
                    const itemSubtotal = Number(so.subtotal) || (Number(so.price || 0) * Number(so.quantity || 1));
                    grouped[oId].total += itemSubtotal;
                    grouped[oId].items.push({
                        id: so.productId,
                        productId: so.productId,
                        name: so.productName || "Product",
                        price: Number(so.price || 0),
                        quantity: Number(so.quantity || 1),
                        subtotal: itemSubtotal,
                        imageUrl: so.imageUrl || ""
                    });
                });
                setStorage(ORDERS_KEY, Object.values(grouped));
            }

            updateSellerUI();

            if (typeof window.renderProducts === "function") window.renderProducts();
            if (typeof window.renderInventory === "function") window.renderInventory();
            if (typeof window.renderOrders === "function") window.renderOrders();
            if (typeof window.renderDashboard === "function") window.renderDashboard();
            if (typeof window.renderAnalytics === "function") window.renderAnalytics();
        } catch (err) {
            console.warn("Backend seller sync notice:", err);
        }
    }

    /* =====================================================
       INITIALIZE
       ===================================================== */

    function initialize() {

        updateSellerUI();

        bindSellerEvents();

        syncSellerFromBackend();

    }


    /* =====================================================
       PUBLIC API
       ===================================================== */

    window.ShopSphereSeller = {

        getCurrentUser:
            getCurrentUser,

        isLoggedIn:
            isLoggedIn,

        getSellerName:
            getSellerName,

        getSellerInitial:
            getSellerInitial,

        getSellerEmail:
            getSellerEmail,

        requireSeller:
            requireSeller,

        getSellerProducts:
            getSellerProducts,

        getMarketplaceProducts:
            getMarketplaceProducts,

        getAllProducts:
            getAllProducts,

        saveSellerProducts:
            saveSellerProducts,

        getProductById:
            getProductById,

        addProduct:
            addProduct,

        updateProduct:
            updateProduct,

        deleteProduct:
            deleteProduct,

        getStock:
            getStock,

        updateStock:
            updateStock,

        getStockStatus:
            getStockStatus,

        getStockLabel:
            getStockLabel,

        getInventoryStats:
            getInventoryStats,

        getOrders:
            getOrders,

        getOrderStatus:
            getOrderStatus,

        getOrderTotal:
            getOrderTotal,

        getOrderItems:
            getOrderItems,

        getProductsSold:
            getProductsSold,

        getSalesStats:
            getSalesStats,

        getOrdersByStatus:
            getOrdersByStatus,

        updateOrderStatus:
            updateOrderStatus,

        getCategorySales:
            getCategorySales,

        getTopProducts:
            getTopProducts,

        formatPrice:
            formatPrice,

        formatDate:
            formatDate,

        showToast:
            showToast,

        updateSellerUI:
            updateSellerUI,

        logout:
            logout,

        getStorage:
            getStorage,

        setStorage:
            setStorage,

        syncSellerFromBackend:
            syncSellerFromBackend
    };


    document.addEventListener(
        "DOMContentLoaded",
        initialize
    );

})();