/* =========================================================
   SHOPSPHERE — CENTRALIZED API CLIENT
   Pure JavaScript client connecting to Spring Boot backend
   ========================================================= */

(function () {
    "use strict";

    const API_BASE = "https://martplus-backend.onrender.com/api";
    const AUTH_KEY = "shopsphere_user";
    
    function getToken() {
        try {
            const user = JSON.parse(localStorage.getItem(AUTH_KEY) || "null");
            return user?.token || null;
        } catch (e) {
            return null;
        }
    }

    function getCurrentUser() {
        try {
            return JSON.parse(localStorage.getItem(AUTH_KEY) || "null");
        } catch (e) {
            return null;
        }
    }

    async function request(endpoint, options = {}) {
        const url = endpoint.startsWith("http") ? endpoint : `${API_BASE}${endpoint.startsWith("/") ? "" : "/"}${endpoint}`;
        const headers = { ...options.headers };

        const token = getToken();
        const isAuthEndpoint = url.includes("/auth/");
        if (!isAuthEndpoint && token && !headers["Authorization"]) {
            headers["Authorization"] = `Bearer ${token}`;
        }

        if (options.body && typeof options.body === "object" && !(options.body instanceof FormData) && !(options.body instanceof URLSearchParams)) {
            headers["Content-Type"] = "application/json";
            options.body = JSON.stringify(options.body);
        }

        options.headers = headers;

        try {
            const response = await fetch(url, options);

            // Handle Unauthorized
            if (response.status === 401) {
                const isAuthEndpoint = url.includes("/api/auth/");
                if (!isAuthEndpoint) {
                    localStorage.removeItem(AUTH_KEY);
                    const isInSubdir = window.location.pathname.includes("/buyer/") ||
                                      window.location.pathname.includes("/seller/") ||
                                      window.location.pathname.includes("/admin/");
                    window.location.href = isInSubdir ? "../login.html" : "login.html";
                    throw new Error("Session expired. Please sign in again.");
                }
            }

            const contentType = response.headers.get("content-type");
            let data;
            if (contentType && contentType.includes("application/json")) {
                data = await response.json();
            } else {
                data = await response.text();
            }

            if (!response.ok) {
                const errorMessage = (typeof data === "object" && data !== null)
                    ? (data.message || JSON.stringify(data))
                    : (data || `Request failed with status ${response.status}`);
                throw new Error(errorMessage);
            }

            return data;
        } catch (error) {
            console.error(`API Error [${options.method || "GET"} ${endpoint}]:`, error);
            throw error;
        }
    }

    const ShopSphereApi = {
        BASE_URL: API_BASE,
        getToken,
        getCurrentUser,
        request,

        // ==================== AUTH ====================
        auth: {
            login: (email, password) => request("/auth/login", {
                method: "POST",
                body: { email, password }
            }),
            register: (userData) => request("/auth/register", {
                method: "POST",
                body: userData
            }),
            sendPhoneOtp: (phone) => request("/auth/send-phone-otp", {
                method: "POST",
                body: { phone }
            }),
            verifyEmail: (target, otp) => request("/auth/verify-email", {
                method: "POST",
                body: { target, otp, purpose: "EMAIL_VERIFICATION" }
            }),
            verifyPhone: (target, otp) => request("/auth/verify-phone", {
                method: "POST",
                body: { target, otp, purpose: "PHONE_VERIFICATION" }
            }),
            verifyLoginOtp: (challengeId, otp) => request("/auth/verify-login-otp", {
                method: "POST",
                body: { challengeId, otp }
            }),
            resendEmailOtp: (target) => request("/auth/resend-email-otp", {
                method: "POST",
                body: { target }
            }),
            resendPhoneOtp: (target) => request("/auth/resend-phone-otp", {
                method: "POST",
                body: { target }
            }),
            resendLoginOtp: (challengeId) => request("/auth/resend-login-otp", {
                method: "POST",
                body: { challengeId }
            }),
            forgotPassword: (email) => request("/auth/forgot-password", {
                method: "POST",
                body: { email }
            }),
            verifyPasswordReset: (email, otp) => request("/auth/verify-password-reset", {
                method: "POST",
                body: { email, otp }
            }),
            resetPassword: (email, resetToken, newPassword, confirmPassword) => request("/auth/reset-password", {
                method: "POST",
                body: { email, resetToken, newPassword, confirmPassword }
            }),
            resendPasswordReset: (email) => request("/auth/resend-password-reset", {
                method: "POST",
                body: { email }
            }),
            getCurrentUser: () => request("/users/me")
        },

        // ==================== PRODUCTS ====================
        products: {
            getAll: () => request("/products"),
            getById: (id) => request(`/products/${id}`),
            getByCategory: (category) => request(`/products/category/${encodeURIComponent(category)}`),
            getMyProducts: () => request("/products/seller/my-products"),
            create: (product) => request("/products", { method: "POST", body: product }),
            update: (id, product) => request(`/products/${id}`, { method: "PUT", body: product }),
            delete: (id) => request(`/products/${id}`, { method: "DELETE" }),
            updateStock: (productId, stock) => request(`/seller/inventory/${productId}/stock?stock=${stock}`, { method: "PUT" }),
            getLowStock: (threshold = 5) => request(`/seller/inventory/low-stock?threshold=${threshold}`)
        },

        // ==================== CATEGORIES ====================
        categories: {
            getAll: () => request("/categories"),
            getById: (id) => request(`/categories/${id}`)
        },

        // ==================== BUYER ====================
        buyer: {
            getDashboard: () => request("/buyer/dashboard"),
            getProfile: () => request("/buyer/profile"),
            updateProfile: (data) => request("/buyer/profile", { method: "PUT", body: data }),
            changePassword: (currentPassword, newPassword) => request("/buyer/profile/change-password", {
                method: "POST",
                body: { currentPassword, newPassword }
            }),

            // Cart
            getCart: () => request("/buyer/cart"),
            addToCart: (productId, quantity = 1) => request(`/buyer/cart?productId=${productId}&quantity=${quantity}`, { method: "POST" }),
            updateCartItem: (cartItemId, quantity) => request(`/buyer/cart/${cartItemId}?quantity=${quantity}`, { method: "PUT" }),
            removeCartItem: (cartItemId) => request(`/buyer/cart/${cartItemId}`, { method: "DELETE" }),
            clearCart: () => request("/buyer/cart", { method: "DELETE" }),

            // Wishlist
            getWishlist: () => request("/buyer/wishlist"),
            addToWishlist: (productId) => request(`/buyer/wishlist?productId=${productId}`, { method: "POST" }),
            removeWishlistItem: (productId) => request(`/buyer/wishlist/${productId}`, { method: "DELETE" }),
            clearWishlist: () => request("/buyer/wishlist", { method: "DELETE" }),

            // Addresses
            getAddresses: () => request("/buyer/addresses"),
            addAddress: (address) => request("/buyer/addresses", { method: "POST", body: address }),
            updateAddress: (id, address) => request(`/buyer/addresses/${id}`, { method: "PUT", body: address }),
            deleteAddress: (id) => request(`/buyer/addresses/${id}`, { method: "DELETE" }),

            // Orders
            placeOrder: (addressId, paymentMethod) => request(`/buyer/orders?addressId=${addressId}&paymentMethod=${encodeURIComponent(paymentMethod)}`, { method: "POST" }),
            getOrders: () => request("/buyer/orders"),
            getOrderById: (orderId) => request(`/buyer/orders/${orderId}`),
            cancelOrder: (orderId) => request(`/buyer/orders/${orderId}/cancel`, { method: "PUT" }),
            getTracking: (orderId) => request(`/buyer/orders/${orderId}/tracking`),

            // Payments
            createPayment: (orderId) => request(`/buyer/payments/order/${orderId}`, { method: "POST" }),
            markPaymentSuccessful: (orderId) => request(`/buyer/payments/order/${orderId}/success`, { method: "PUT" }),
            getPayment: (orderId) => request(`/buyer/payments/order/${orderId}`)
        },

        // ==================== SELLER ====================
        seller: {
            getDashboard: () => request("/seller/dashboard"),
            getOrders: () => request("/seller/orders"),
            getAnalytics: () => request("/seller/analytics")
        },

        // ==================== ADMIN ====================
        admin: {
            getDashboard: () => request("/admin/dashboard"),
            getUsers: () => request("/admin/users"),
            getUserById: (id) => request(`/admin/users/${id}`),
            createUser: (userData) => request("/admin/users", { method: "POST", body: userData }),
            updateUser: (id, userData) => request(`/admin/users/${id}`, { method: "PUT", body: userData }),
            deactivateUser: (id) => request(`/admin/users/${id}/deactivate`, { method: "PUT" }),

            // Products
            getProducts: () => request("/admin/products"),
            createProduct: (product) => request("/admin/products", { method: "POST", body: product }),
            updateProduct: (id, product) => request(`/admin/products/${id}`, { method: "PUT", body: product }),
            deleteProduct: (id) => request(`/admin/products/${id}`, { method: "DELETE" }),

            // Orders
            getOrders: () => request("/admin/orders"),
            getOrderById: (orderId) => request(`/admin/orders/${orderId}`),
            updateOrderStatus: (orderId, status) => request(`/admin/orders/${orderId}/status?status=${encodeURIComponent(status)}`, { method: "PUT" }),
            cancelOrder: (orderId) => request(`/admin/orders/${orderId}/cancel`, { method: "PUT" }),
            updateTracking: (orderId, status, description = "") => request(`/admin/orders/${orderId}/tracking?status=${encodeURIComponent(status)}&description=${encodeURIComponent(description)}`, { method: "PUT" }),

            // Activity Logs
            getAllActivity: () => request("/admin/activity"),
            getMyActivity: () => request("/admin/activity/my")
        }
    };

    window.ShopSphereApi = ShopSphereApi;
})();
