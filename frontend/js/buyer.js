/* =========================================================
   ShopSphere - Buyer Shared JavaScript
   ========================================================= */

(function () {

    "use strict";


    const AUTH_KEY = "shopsphere_user";
    const CART_KEY = "shopsphere_cart";
    const WISHLIST_KEY = "shopsphere_wishlist";
    const ORDERS_KEY = "shopsphere_orders";
    const ADDRESSES_KEY = "shopsphere_addresses";
    const PAYMENTS_KEY = "shopsphere_payments";


    /* =====================================================
       STORAGE HELPERS
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
                "Unable to save ShopSphere data:",
                error
            );

            return false;
        }
    }


    /* =====================================================
       CURRENT USER
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


    function getBuyerName() {

        const user =
            getCurrentUser();

        if (!user) {
            return "Buyer";
        }

        if (user.name) {
            return user.name;
        }

        const fullName = (
            (user.firstName || "") +
            " " +
            (user.lastName || "")
        ).trim();

        return fullName || "Buyer";
    }


    function getBuyerInitial() {

        const name =
            getBuyerName();

        return name
            .charAt(0)
            .toUpperCase();
    }


    function getBuyerEmail() {

        const user =
            getCurrentUser();

        return user?.email || "";
    }


    /* =====================================================
       CART
       ===================================================== */

    function getCart() {

        const cart =
            getStorage(
                CART_KEY,
                []
            );

        return Array.isArray(cart)
            ? cart
            : [];
    }


    function getCartCount() {

        return getCart().reduce(
            function (total, item) {

                return total +
                    Number(
                        item.quantity || 1
                    );

            },
            0
        );
    }


    function getCartSubtotal() {

        return getCart().reduce(
            function (total, item) {

                const price =
                    Number(item.price || 0);

                const quantity =
                    Number(item.quantity || 1);

                return total +
                    (price * quantity);

            },
            0
        );
    }


    function updateCartCount() {

        const count =
            getCartCount();

        document
            .querySelectorAll(
                "#cartCount, [data-cart-count]"
            )
            .forEach(function (element) {

                element.textContent =
                    count;

            });

        return count;
    }


    /* =====================================================
       WISHLIST
       ===================================================== */

    function getWishlist() {

        const wishlist =
            getStorage(
                WISHLIST_KEY,
                []
            );

        return Array.isArray(wishlist)
            ? wishlist
            : [];
    }


    function getWishlistProductIds() {

        return getWishlist()
            .map(function (item) {

                if (
                    typeof item === "object" &&
                    item !== null
                ) {
                    return Number(
                        item.productId ??
                        item.id
                    );
                }

                return Number(item);

            })
            .filter(function (id) {

                return Number.isFinite(id);

            });
    }


    function getWishlistCount() {

        return getWishlistProductIds()
            .length;
    }


    function updateWishlistCount() {

        const count =
            getWishlistCount();

        document
            .querySelectorAll(
                "#wishlistCount, [data-wishlist-count]"
            )
            .forEach(function (element) {

                element.textContent =
                    count;

            });

        return count;
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


    function getOrderCount() {

        return getOrders()
            .length;
    }


    function getOrderTotal() {

        return getOrders().reduce(
            function (total, order) {

                return total +
                    Number(
                        order.total ??
                        order.grandTotal ??
                        0
                    );

            },
            0
        );
    }


    function getLatestOrder() {

        const orders =
            getOrders();

        if (!orders.length) {
            return null;
        }

        return orders
            .slice()
            .sort(function (a, b) {

                const dateA =
                    new Date(
                        a.createdAt ||
                        a.orderDate ||
                        a.date ||
                        0
                    );

                const dateB =
                    new Date(
                        b.createdAt ||
                        b.orderDate ||
                        b.date ||
                        0
                    );

                return dateB - dateA;

            })[0];
    }


    /* =====================================================
       ADDRESSES
       ===================================================== */

    function getAddresses() {

        const addresses =
            getStorage(
                ADDRESSES_KEY,
                []
            );

        return Array.isArray(addresses)
            ? addresses
            : [];
    }


    function getDefaultAddress() {

        const addresses =
            getAddresses();

        return (
            addresses.find(function (address) {

                return address.isDefault === true;

            }) ||
            addresses[0] ||
            null
        );
    }


    /* =====================================================
       PAYMENTS
       ===================================================== */

    function getPayments() {

        const payments =
            getStorage(
                PAYMENTS_KEY,
                []
            );

        return Array.isArray(payments)
            ? payments
            : [];
    }


    function getDefaultPayment() {

        const payments =
            getPayments();

        return (
            payments.find(function (payment) {

                return payment.isDefault === true;

            }) ||
            payments[0] ||
            null
        );
    }


    /* =====================================================
       DASHBOARD STATS
       ===================================================== */

    function getDashboardStats() {

        const orders =
            getOrders();

        const cart =
            getCart();

        const wishlist =
            getWishlist();

        return {

            orders:
                orders.length,

            cartItems:
                cart.reduce(
                    function (total, item) {

                        return total +
                            Number(
                                item.quantity || 1
                            );

                    },
                    0
                ),

            wishlist:
                getWishlistProductIds()
                    .length,

            totalSpent:
                getOrderTotal(),

            addresses:
                getAddresses().length,

            payments:
                getPayments().length

        };
    }


    /* =====================================================
       FORMAT PRICE
       ===================================================== */

    function formatPrice(value) {

        const number =
            Number(value || 0);

        return "₹" +
            number.toLocaleString(
                "en-IN"
            );
    }


    /* =====================================================
       FORMAT DATE
       ===================================================== */

    function formatDate(dateValue) {

        if (!dateValue) {
            return "N/A";
        }

        const date =
            new Date(dateValue);

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
       ORDER STATUS
       ===================================================== */

    function normalizeOrderStatus(status) {

        const value =
            String(
                status || "Processing"
            )
            .trim()
            .toLowerCase();

        if (
            value === "out for delivery" ||
            value === "out_for_delivery"
        ) {
            return "Out for Delivery";
        }

        if (value === "shipped") {
            return "Shipped";
        }

        if (value === "delivered") {
            return "Delivered";
        }

        if (value === "cancelled") {
            return "Cancelled";
        }

        return "Processing";
    }


    function getStatusClass(status) {

        const normalized =
            normalizeOrderStatus(
                status
            );

        if (normalized === "Delivered") {
            return "success";
        }

        if (
            normalized === "Shipped" ||
            normalized === "Out for Delivery"
        ) {
            return "info";
        }

        if (normalized === "Cancelled") {
            return "danger";
        }

        return "warning";
    }


    /* =====================================================
       RECENTLY VIEWED
       ===================================================== */

    function getRecentlyViewed() {

        const viewed =
            getStorage(
                "shopsphere_recently_viewed",
                []
            );

        return Array.isArray(viewed)
            ? viewed
            : [];
    }


    function addRecentlyViewed(productId) {

        const id =
            Number(productId);

        if (!Number.isFinite(id)) {
            return;
        }

        let viewed =
            getRecentlyViewed();

        viewed =
            viewed.filter(function (item) {

                const existingId =
                    typeof item === "object"
                        ? Number(
                            item.productId ??
                            item.id
                        )
                        : Number(item);

                return existingId !== id;

            });


        viewed.unshift({
            productId: id,
            viewedAt:
                new Date().toISOString()
        });


        viewed =
            viewed.slice(0, 12);

        setStorage(
            "shopsphere_recently_viewed",
            viewed
        );
    }


    /* =====================================================
       PRODUCT HELPER
       ===================================================== */

    function getProduct(productId) {

        const id =
            Number(productId);

        if (
            window.ShopSphereProducts &&
            typeof window.ShopSphereProducts
                .getProductById === "function"
        ) {

            return window.ShopSphereProducts
                .getProductById(id);

        }

        return null;
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


        setTimeout(function () {

            toast.classList.remove(
                "show"
            );

        }, 2200);
    }


    /* =====================================================
       BUYER UI
       ===================================================== */

    function updateBuyerUI() {

        const name =
            getBuyerName();

        const initial =
            getBuyerInitial();

        const email =
            getBuyerEmail();


        document
            .querySelectorAll(
                "[data-buyer-name]"
            )
            .forEach(function (element) {

                element.textContent =
                    name;

            });


        document
            .querySelectorAll(
                "[data-buyer-email]"
            )
            .forEach(function (element) {

                element.textContent =
                    email;

            });


        document
            .querySelectorAll(
                "[data-buyer-initial]"
            )
            .forEach(function (element) {

                element.textContent =
                    initial;

            });


        updateCartCount();
        updateWishlistCount();
    }


    /* =====================================================
       REQUIRE BUYER
       ===================================================== */

    function requireBuyer() {

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


        if (role !== "buyer") {

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

            user.loggedIn = false;

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

    function bindBuyerEvents() {

        document
            .querySelectorAll(
                "[data-buyer-logout]"
            )
            .forEach(function (button) {

                button.addEventListener(
                    "click",
                    function (event) {

                        event.preventDefault();

                        logout();

                    }
                );

            });


        document
            .querySelectorAll(
                "[data-add-recent]"
            )
            .forEach(function (element) {

                element.addEventListener(
                    "click",
                    function () {

                        addRecentlyViewed(
                            element.dataset.addRecent
                        );

                    }
                );

            });
    }


    /* =====================================================
       BACKEND SYNC & ACTIONS
       ===================================================== */

    async function syncBuyerFromBackend() {
        if (!window.ShopSphereAuth || !window.ShopSphereAuth.isBuyer()) return;
        const api = window.ShopSphereApi;
        if (!api) return;

        try {
            // 1. Orders
            const backendOrders = await api.buyer.getOrders().catch(() => null);
            if (Array.isArray(backendOrders)) {
                const mappedOrders = backendOrders.map(bo => ({
                    id: bo.id,
                    orderNumber: "SS" + bo.id.toString().padStart(6, "0"),
                    date: bo.createdAt,
                    createdAt: bo.createdAt,
                    status: bo.status,
                    payment: bo.paymentMethod,
                    paymentStatus: bo.paymentStatus,
                    total: Number(bo.totalAmount) || 0,
                    shippingAddressId: bo.shippingAddressId,
                    items: (bo.items || []).map(bi => ({
                        id: bi.productId,
                        productId: bi.productId,
                        name: bi.productName,
                        price: Number(bi.price),
                        quantity: bi.quantity,
                        subtotal: Number(bi.subtotal),
                        imageUrl: bi.imageUrl,
                        seller: "Verified Seller"
                    }))
                }));
                setStorage(ORDERS_KEY, mappedOrders);
            }

            // 2. Addresses
            const backendAddresses = await api.buyer.getAddresses().catch(() => null);
            if (Array.isArray(backendAddresses)) {
                const mappedAddresses = backendAddresses.map(ba => ({
                    id: ba.id,
                    fullName: ba.fullName,
                    phone: ba.phone,
                    addressLine: ba.addressLine,
                    address: ba.addressLine,
                    city: ba.city,
                    state: ba.state,
                    pincode: ba.pincode,
                    isDefault: !!ba.defaultAddress
                }));
                setStorage(ADDRESSES_KEY, mappedAddresses);
            }

            // 3. Cart
            const backendCart = await api.buyer.getCart().catch(() => null);
            if (Array.isArray(backendCart)) {
                const mappedCart = backendCart.map(item => ({
                    cartItemId: item.id,
                    productId: item.productId,
                    name: item.productName,
                    price: Number(item.price),
                    quantity: item.quantity,
                    seller: "Verified Seller"
                }));
                setStorage(CART_KEY, mappedCart);
            }

            updateBuyerUI();

            if (typeof window.renderOrders === "function") window.renderOrders();
            if (typeof window.renderAddresses === "function") window.renderAddresses();
            if (typeof window.updateStats === "function") window.updateStats();
            if (typeof window.renderRecentOrders === "function") window.renderRecentOrders();
        } catch (err) {
            console.warn("Backend buyer sync notice:", err);
        }
    }

    async function addAddress(addressData) {
        if (window.ShopSphereAuth && window.ShopSphereAuth.isBuyer()) {
            const api = window.ShopSphereApi;
            if (api) {
                const saved = await api.buyer.addAddress({
                    fullName: addressData.fullName || addressData.name || "Customer",
                    phone: addressData.phone || "9876543210",
                    addressLine: addressData.addressLine || addressData.address || addressData.street || "Main Street",
                    city: addressData.city || "City",
                    state: addressData.state || "State",
                    pincode: addressData.pincode || addressData.zipCode || addressData.zip || "560001",
                    defaultAddress: !!(addressData.defaultAddress ?? addressData.isDefault)
                });
                await syncBuyerFromBackend();
                return saved;
            }
        }
    }

    async function deleteAddress(addressId) {
        if (window.ShopSphereAuth && window.ShopSphereAuth.isBuyer()) {
            const api = window.ShopSphereApi;
            if (api) {
                await api.buyer.deleteAddress(addressId);
                await syncBuyerFromBackend();
            }
        }
    }

    async function cancelOrder(orderId) {
        if (window.ShopSphereAuth && window.ShopSphereAuth.isBuyer()) {
            const api = window.ShopSphereApi;
            if (api) {
                const res = await api.buyer.cancelOrder(orderId);
                await syncBuyerFromBackend();
                return res;
            }
        }
    }

    /* =====================================================
       INITIALIZE
       ===================================================== */

    function initialize() {

        updateBuyerUI();
        bindBuyerEvents();
        syncBuyerFromBackend();

    }


    /* =====================================================
       PUBLIC API
       ===================================================== */

    window.ShopSphereBuyer = {

        getCurrentUser:
            getCurrentUser,

        isLoggedIn:
            isLoggedIn,

        getBuyerName:
            getBuyerName,

        getBuyerInitial:
            getBuyerInitial,

        getBuyerEmail:
            getBuyerEmail,

        getCart:
            getCart,

        getCartCount:
            getCartCount,

        getCartSubtotal:
            getCartSubtotal,

        updateCartCount:
            updateCartCount,

        getWishlist:
            getWishlist,

        getWishlistProductIds:
            getWishlistProductIds,

        getWishlistCount:
            getWishlistCount,

        updateWishlistCount:
            updateWishlistCount,

        getOrders:
            getOrders,

        getOrderCount:
            getOrderCount,

        getOrderTotal:
            getOrderTotal,

        getLatestOrder:
            getLatestOrder,

        getAddresses:
            getAddresses,

        getDefaultAddress:
            getDefaultAddress,

        getPayments:
            getPayments,

        getDefaultPayment:
            getDefaultPayment,

        getDashboardStats:
            getDashboardStats,

        getRecentlyViewed:
            getRecentlyViewed,

        addRecentlyViewed:
            addRecentlyViewed,

        getProduct:
            getProduct,

        formatPrice:
            formatPrice,

        formatDate:
            formatDate,

        normalizeOrderStatus:
            normalizeOrderStatus,

        getStatusClass:
            getStatusClass,

        showToast:
            showToast,

        updateBuyerUI:
            updateBuyerUI,

        requireBuyer:
            requireBuyer,

        logout:
            logout,

        getStorage:
            getStorage,

        setStorage:
            setStorage,

        syncBuyerFromBackend:
            syncBuyerFromBackend,

        addAddress:
            addAddress,

        deleteAddress:
            deleteAddress,

        cancelOrder:
            cancelOrder
    };


    document.addEventListener(
        "DOMContentLoaded",
        initialize
    );

})();