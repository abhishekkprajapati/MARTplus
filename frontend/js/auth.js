var AUTH_STORAGE_KEY = window.AUTH_STORAGE_KEY || "shopsphere_user";
window.AUTH_STORAGE_KEY = AUTH_STORAGE_KEY;

/* ================================
   Authentication Helpers
================================ */

function getCurrentUser() {
    try {
        const user = JSON.parse(
            localStorage.getItem(AUTH_STORAGE_KEY) || "null"
        );
        return user && typeof user === "object" ? user : null;
    } catch (error) {
        return null;
    }
}

function getToken() {
    const user = getCurrentUser();
    return user?.token || null;
}

function isLoggedIn() {
    const user = getCurrentUser();
    return !!(user && user.token);
}

function getUserRole() {
    const user = getCurrentUser();
    return user?.role ? String(user.role).toLowerCase() : null;
}

function getUserRoleUpper() {
    const user = getCurrentUser();
    return user?.role ? String(user.role).toUpperCase() : null;
}

/* ================================
   Login Session
================================ */

function createSession(userData) {
    const roleUpper = (userData.role || "BUYER").toUpperCase();
    const roleLower = roleUpper.toLowerCase();
    const id = userData.userId || userData.id;
    const name = userData.name || (
        ((userData.firstName || "") + " " + (userData.lastName || "")).trim() ||
        userData.email ||
        "User"
    );
    const session = {
        ...userData,
        id: id,
        userId: id,
        name: name,
        role: roleLower,
        roleUpper: roleUpper,
        loggedIn: true,
        loginTime: new Date().toISOString()
    };

    localStorage.setItem(
        AUTH_STORAGE_KEY,
        JSON.stringify(session)
    );

    return session;
}

/* ================================
   Logout
================================ */

function logout() {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    sessionStorage.removeItem("shopsphere_redirect_after_login");

    const isInSubdir =
        window.location.pathname.includes("/buyer/") ||
        window.location.pathname.includes("/seller/") ||
        window.location.pathname.includes("/admin/");

    window.location.href = isInSubdir ? "../login.html" : "login.html";
}

/* ================================
   Clear Session
================================ */

function clearSession() {
    localStorage.removeItem(AUTH_STORAGE_KEY);
}

/* ================================
   Role-Based Dashboard
================================ */

function getDashboardPath(role) {
    const r = (role || "").toUpperCase();
    const isInSubdir =
        window.location.pathname.includes("/buyer/") ||
        window.location.pathname.includes("/seller/") ||
        window.location.pathname.includes("/admin/");
    const prefix = isInSubdir ? "../" : "";

    switch (r) {
        case "ADMIN":
            return prefix + "admin/dashboard.html";
        case "SELLER":
            return prefix + "seller/dashboard.html";
        case "BUYER":
            return prefix + "products.html";
        default:
            return prefix + "index.html";
    }
}

function redirectToDashboard() {
    const user = getCurrentUser();
    if (!user || !user.role) {
        const isInSubdir =
            window.location.pathname.includes("/buyer/") ||
            window.location.pathname.includes("/seller/") ||
            window.location.pathname.includes("/admin/");
        window.location.href = isInSubdir ? "../index.html" : "index.html";
        return;
    }
    window.location.href = getDashboardPath(user.role);
}

/* ================================
   Protected Page
=============================== */

function requireLogin() {
    if (!isLoggedIn()) {
        const currentPage = window.location.pathname.split("/").pop();
        if (currentPage && currentPage !== "login.html" && currentPage !== "register.html") {
            sessionStorage.setItem("shopsphere_redirect_after_login", currentPage);
        }
        const isInSubdir =
            window.location.pathname.includes("/buyer/") ||
            window.location.pathname.includes("/seller/") ||
            window.location.pathname.includes("/admin/");
        window.location.href = isInSubdir ? "../login.html" : "login.html";
        return false;
    }
    return true;
}

/* ================================
   Role Protection
=============================== */

function requireRole(requiredRole) {
    if (!requireLogin()) {
        return false;
    }

    const currentRole = (getUserRole() || "").toUpperCase();
    if (currentRole !== (requiredRole || "").toUpperCase()) {
        redirectToDashboard();
        return false;
    }

    return true;
}

/* ================================
   Role Check Helpers
=============================== */

function isBuyer() {
    return isLoggedIn() && (getUserRole() || "").toUpperCase() === "BUYER";
}

function isSeller() {
    return isLoggedIn() && (getUserRole() || "").toUpperCase() === "SELLER";
}

function isAdmin() {
    return isLoggedIn() && (getUserRole() || "").toUpperCase() === "ADMIN";
}

/* ================================
   Redirect After Login
=============================== */

function handlePostLoginRedirect() {
    const redirectPage = sessionStorage.getItem("shopsphere_redirect_after_login");
    if (redirectPage) {
        sessionStorage.removeItem("shopsphere_redirect_after_login");
        if (redirectPage !== "buyer/dashboard.html" && redirectPage !== "dashboard.html") {
            window.location.href = redirectPage;
            return true;
        }
    }
    return false;
}

/* ================================
   Helper Utilities for Auth UI
=============================== */

function escapeHtml(text) {
    if (!text) return "";
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function getDisplayName(user) {
    if (!user) return "Account";
    if (user.firstName && user.firstName.trim()) {
        return user.firstName.trim();
    }
    if (user.name && user.name.trim()) {
        const parts = user.name.trim().split(" ");
        return parts[0] || user.name.trim();
    }
    if (user.email) {
        return user.email.split("@")[0];
    }
    return "Account";
}

function getFullName(user) {
    if (!user) return "User";
    if (user.name && user.name.trim()) {
        return user.name.trim();
    }
    const full = ((user.firstName || "") + " " + (user.lastName || "")).trim();
    if (full) return full;
    if (user.email) return user.email;
    return "User";
}

function closeAccountSidebar() {
    const sidebar = document.getElementById("accountSidebar");
    const overlay = document.getElementById("accountSidebarOverlay");
    const btn = document.getElementById("headerAccountAction");

    if (sidebar) {
        sidebar.classList.remove("open");
        sidebar.setAttribute("aria-hidden", "true");
    }
    if (overlay) {
        overlay.classList.remove("active");
        overlay.setAttribute("aria-hidden", "true");
    }
    if (btn) {
        btn.setAttribute("aria-expanded", "false");
    }
    document.body.style.overflow = "";
}

function openAccountSidebar() {
    const user = getCurrentUser();
    const isInSubdir =
        window.location.pathname.includes("/buyer/") ||
        window.location.pathname.includes("/seller/") ||
        window.location.pathname.includes("/admin/");
    const prefix = isInSubdir ? "../" : "";

    let sidebar = document.getElementById("accountSidebar");
    let overlay = document.getElementById("accountSidebarOverlay");

    if (!sidebar || !overlay) {
        setupBuyerAccountSidebar(user, prefix);
        sidebar = document.getElementById("accountSidebar");
        overlay = document.getElementById("accountSidebarOverlay");
    }

    if (!sidebar || !overlay) return;

    sidebar.classList.add("open");
    overlay.classList.add("active");
    sidebar.setAttribute("aria-hidden", "false");
    overlay.setAttribute("aria-hidden", "false");
    const btn = document.getElementById("headerAccountAction");
    if (btn) {
        btn.setAttribute("aria-expanded", "true");
    }
    document.body.style.overflow = "hidden";
}

function toggleAccountSidebar() {
    const sidebar = document.getElementById("accountSidebar");
    if (sidebar && sidebar.classList.contains("open")) {
        closeAccountSidebar();
    } else {
        openAccountSidebar();
    }
}

function setupBuyerAccountSidebar(user, prefix) {
    if (!user) return;

    let overlay = document.getElementById("accountSidebarOverlay");
    let sidebar = document.getElementById("accountSidebar");

    const displayName = getDisplayName(user);
    const fullName = getFullName(user);
    const userEmail = user.email || "";
    const avatarInitial = (displayName || "B").charAt(0).toUpperCase();

    if (!overlay) {
        overlay = document.createElement("div");
        overlay.className = "account-sidebar-overlay";
        overlay.id = "accountSidebarOverlay";
        overlay.setAttribute("aria-hidden", "true");
        document.body.appendChild(overlay);
    }

    if (!sidebar) {
        sidebar = document.createElement("aside");
        sidebar.className = "account-sidebar";
        sidebar.id = "accountSidebar";
        sidebar.setAttribute("role", "dialog");
        sidebar.setAttribute("aria-modal", "true");
        sidebar.setAttribute("aria-label", "Buyer Account Menu");
        sidebar.setAttribute("aria-hidden", "true");
        document.body.appendChild(sidebar);
    }

    // Keep contents updated with user data and relative paths
    sidebar.innerHTML = `
        <div class="account-sidebar-header">
            <h2 class="account-sidebar-title">
                <span>Account</span>
            </h2>
            <button type="button" class="account-sidebar-close" id="accountSidebarClose" aria-label="Close account menu">✕</button>
        </div>
        <div class="account-sidebar-user">
            <div class="account-sidebar-avatar">${escapeHtml(avatarInitial)}</div>
            <div class="account-sidebar-info">
                <h3 class="account-sidebar-name">${escapeHtml(fullName)}</h3>
                <p class="account-sidebar-email">${escapeHtml(userEmail)}</p>
                <span class="account-sidebar-badge">Buyer Account</span>
            </div>
        </div>
        <ul class="account-sidebar-menu">
            <li class="account-sidebar-item">
                <a href="${prefix}buyer/dashboard.html" class="account-sidebar-link">
                    <span class="account-sidebar-icon">🏠</span>
                    <span class="account-sidebar-label">Dashboard</span>
                    <span class="account-sidebar-arrow">›</span>
                </a>
            </li>
            <li class="account-sidebar-item">
                <a href="${prefix}buyer/profile.html" class="account-sidebar-link">
                    <span class="account-sidebar-icon">👤</span>
                    <span class="account-sidebar-label">My Profile</span>
                    <span class="account-sidebar-arrow">›</span>
                </a>
            </li>
            <li class="account-sidebar-item">
                <a href="${prefix}buyer/orders.html" class="account-sidebar-link">
                    <span class="account-sidebar-icon">📦</span>
                    <span class="account-sidebar-label">My Orders</span>
                    <span class="account-sidebar-arrow">›</span>
                </a>
            </li>
            <li class="account-sidebar-item">
                <a href="${prefix}buyer/tracking.html" class="account-sidebar-link">
                    <span class="account-sidebar-icon">🚚</span>
                    <span class="account-sidebar-label">Track Order</span>
                    <span class="account-sidebar-arrow">›</span>
                </a>
            </li>
            <li class="account-sidebar-item">
                <a href="${prefix}buyer/addresses.html" class="account-sidebar-link">
                    <span class="account-sidebar-icon">📍</span>
                    <span class="account-sidebar-label">My Addresses</span>
                    <span class="account-sidebar-arrow">›</span>
                </a>
            </li>
        </ul>
        <div class="account-sidebar-footer">
            <button type="button" class="account-sidebar-logout-btn" id="sidebarLogoutBtn">
                <span class="account-sidebar-icon">🚪</span>
                <span>Logout</span>
            </button>
        </div>
    `;

    // Global listener for clicks and escape key (attached once)
    if (!window._accountSidebarDocListenerBound) {
        document.addEventListener("click", function (event) {
            const accountBtn = event.target.closest("#headerAccountAction");
            if (accountBtn) {
                if (accountBtn.tagName === "A") return; // Logged-out sign in link
                event.preventDefault();
                event.stopPropagation();
                toggleAccountSidebar();
                return;
            }

            const closeBtn = event.target.closest("#accountSidebarClose");
            if (closeBtn) {
                event.preventDefault();
                event.stopPropagation();
                closeAccountSidebar();
                return;
            }

            const overlayEl = event.target.closest("#accountSidebarOverlay");
            if (overlayEl) {
                event.preventDefault();
                event.stopPropagation();
                closeAccountSidebar();
                return;
            }

            const link = event.target.closest(".account-sidebar-link");
            if (link) {
                closeAccountSidebar();
                return;
            }

            const logoutBtn = event.target.closest("#sidebarLogoutBtn");
            if (logoutBtn) {
                event.preventDefault();
                event.stopPropagation();
                closeAccountSidebar();
                logout();
                return;
            }

            const openSidebar = document.getElementById("accountSidebar");
            if (openSidebar && openSidebar.classList.contains("open")) {
                if (!openSidebar.contains(event.target)) {
                    closeAccountSidebar();
                }
            }
        });

        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape") {
                const openSidebar = document.getElementById("accountSidebar");
                if (openSidebar && openSidebar.classList.contains("open")) {
                    closeAccountSidebar();
                }
            }
        });

        window._accountSidebarDocListenerBound = true;
    }
}

function removeBuyerAccountSidebar() {
    const sidebar = document.getElementById("accountSidebar");
    const overlay = document.getElementById("accountSidebarOverlay");
    if (sidebar) sidebar.remove();
    if (overlay) overlay.remove();
    document.body.style.overflow = "";
}

/* ================================
   Auth UI
=============================== */

function updateAuthUI() {
    const user = getCurrentUser();
    const loggedIn = isLoggedIn();
    const role = (getUserRole() || "").toUpperCase();

    const isInSubdir =
        window.location.pathname.includes("/buyer/") ||
        window.location.pathname.includes("/seller/") ||
        window.location.pathname.includes("/admin/");
    const prefix = isInSubdir ? "../" : "";

    // Legacy data-auth-account elements
    const accountElements = document.querySelectorAll("[data-auth-account]");
    accountElements.forEach(element => {
        if (loggedIn && user) {
            const name = user.name || user.firstName || user.email || "Account";
            element.textContent = name;
        } else {
            element.textContent = "Account";
        }
    });

    // Primary header account action target on shopping pages
    const accountTarget =
        document.getElementById("headerAccountAction") ||
        document.querySelector(".header-actions .header-account-btn") ||
        document.querySelector(".header-actions a[href*='login.html']");

    if (accountTarget) {
        if (!loggedIn) {
            // Logged out state: Sign In link
            removeBuyerAccountSidebar();
            accountTarget.outerHTML = `
                <a href="${prefix}login.html" class="header-action header-account-btn" id="headerAccountAction" aria-label="Sign in to your account">
                    <span class="action-icon">👤</span>
                    <span class="action-text">
                        <small>Hello, Sign in</small>
                        <strong>Account</strong>
                    </span>
                </a>
            `;
        } else if (role === "BUYER") {
            // Logged in as BUYER: Single Account button that triggers right-side drawer
            const displayName = getDisplayName(user);

            accountTarget.outerHTML = `
                <button type="button" class="header-action header-account-btn" id="headerAccountAction" aria-label="Open buyer account menu" aria-haspopup="dialog" aria-expanded="false" title="Account Menu" onclick="event.preventDefault(); event.stopPropagation(); if (window.ShopSphereAuth && window.ShopSphereAuth.toggleAccountSidebar) { window.ShopSphereAuth.toggleAccountSidebar(); }">
                    <span class="action-icon">👤</span>
                    <span class="action-text">
                        <small>Hello, ${escapeHtml(displayName)}</small>
                        <strong>Account <span class="account-caret">▾</span></strong>
                    </span>
                </button>
            `;

            // Setup right-side drawer immediately
            setupBuyerAccountSidebar(user, prefix);

            const newBtn = document.getElementById("headerAccountAction");
            if (newBtn) {
                newBtn.onclick = function (e) {
                    e.preventDefault();
                    e.stopPropagation();
                    toggleAccountSidebar();
                };
            }
        } else if (role === "SELLER") {
            // Logged in as SELLER: Portal link
            removeBuyerAccountSidebar();
            accountTarget.outerHTML = `
                <a href="${prefix}seller/dashboard.html" class="header-action header-account-btn" id="headerAccountAction" title="Seller Dashboard">
                    <span class="action-icon">🏪</span>
                    <span class="action-text">
                        <small>Seller Portal</small>
                        <strong>Dashboard</strong>
                    </span>
                </a>
            `;
        } else if (role === "ADMIN") {
            // Logged in as ADMIN: Panel link
            removeBuyerAccountSidebar();
            accountTarget.outerHTML = `
                <a href="${prefix}admin/dashboard.html" class="header-action header-account-btn" id="headerAccountAction" title="Admin Dashboard">
                    <span class="action-icon">⚙️</span>
                    <span class="action-text">
                        <small>Admin Panel</small>
                        <strong>Dashboard</strong>
                    </span>
                </a>
            `;
        }
    }

    const logoutButtons = document.querySelectorAll("[data-auth-logout]");
    logoutButtons.forEach(button => {
        button.onclick = function (event) {
            event.preventDefault();
            logout();
        };
    });
}

/* ================================
   Global Auth Object
================================ */

window.ShopSphereAuth = {
    getCurrentUser,
    getToken,
    isLoggedIn,
    getUserRole,
    createSession,
    logout,
    clearSession,
    getDashboardPath,
    redirectToDashboard,
    requireLogin,
    requireRole,
    isBuyer,
    isSeller,
    isAdmin,
    handlePostLoginRedirect,
    openAccountSidebar,
    closeAccountSidebar,
    toggleAccountSidebar,
    updateAuthUI
};

/* ================================
   Centralized API Client fallback
================================ */
if (!window.ShopSphereApi) {
    const API_BASE = "http://localhost:8080/api";

    async function apiFetch(endpoint, options = {}) {
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

            if (response.status === 401) {
                const isAuthEndpoint = url.includes("/api/auth/");
                if (!isAuthEndpoint) {
                    logout();
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

    window.ShopSphereApi = {
        BASE_URL: API_BASE,
        getToken,
        getCurrentUser,
        request: apiFetch,
        auth: {
            login: (email, password) => apiFetch("/auth/login", { method: "POST", body: { email, password } }),
            register: (userData) => apiFetch("/auth/register", { method: "POST", body: userData }),
            getCurrentUser: () => apiFetch("/users/me")
        },
        products: {
            getAll: () => apiFetch("/products"),
            getById: (id) => apiFetch(`/products/${id}`),
            getByCategory: (category) => apiFetch(`/products/category/${encodeURIComponent(category)}`),
            getMyProducts: () => apiFetch("/products/seller/my-products"),
            create: (product) => apiFetch("/products", { method: "POST", body: product }),
            update: (id, product) => apiFetch(`/products/${id}`, { method: "PUT", body: product }),
            delete: (id) => apiFetch(`/products/${id}`, { method: "DELETE" }),
            updateStock: (productId, stock) => apiFetch(`/seller/inventory/${productId}/stock?stock=${stock}`, { method: "PUT" }),
            getLowStock: (threshold = 5) => apiFetch(`/seller/inventory/low-stock?threshold=${threshold}`)
        },
        categories: {
            getAll: () => apiFetch("/categories"),
            getById: (id) => apiFetch(`/categories/${id}`)
        },
        buyer: {
            getDashboard: () => apiFetch("/buyer/dashboard"),
            getProfile: () => apiFetch("/buyer/profile"),
            updateProfile: (data) => apiFetch("/buyer/profile", { method: "PUT", body: data }),
            changePassword: (currentPassword, newPassword) => apiFetch("/buyer/profile/change-password", {
                method: "POST",
                body: { currentPassword, newPassword }
            }),
            getCart: () => apiFetch("/buyer/cart"),
            addToCart: (productId, quantity = 1) => apiFetch(`/buyer/cart?productId=${productId}&quantity=${quantity}`, { method: "POST" }),
            updateCartItem: (cartItemId, quantity) => apiFetch(`/buyer/cart/${cartItemId}?quantity=${quantity}`, { method: "PUT" }),
            removeCartItem: (cartItemId) => apiFetch(`/buyer/cart/${cartItemId}`, { method: "DELETE" }),
            clearCart: () => apiFetch("/buyer/cart", { method: "DELETE" }),
            getWishlist: () => apiFetch("/buyer/wishlist"),
            addToWishlist: (productId) => apiFetch(`/buyer/wishlist?productId=${productId}`, { method: "POST" }),
            removeWishlistItem: (productId) => apiFetch(`/buyer/wishlist/${productId}`, { method: "DELETE" }),
            clearWishlist: () => apiFetch("/buyer/wishlist", { method: "DELETE" }),
            getAddresses: () => apiFetch("/buyer/addresses"),
            addAddress: (address) => apiFetch("/buyer/addresses", { method: "POST", body: address }),
            updateAddress: (id, address) => apiFetch(`/buyer/addresses/${id}`, { method: "PUT", body: address }),
            deleteAddress: (id) => apiFetch(`/buyer/addresses/${id}`, { method: "DELETE" }),
            placeOrder: (addressId, paymentMethod) => apiFetch(`/buyer/orders?addressId=${addressId}&paymentMethod=${encodeURIComponent(paymentMethod)}`, { method: "POST" }),
            getOrders: () => apiFetch("/buyer/orders"),
            getOrderById: (orderId) => apiFetch(`/buyer/orders/${orderId}`),
            cancelOrder: (orderId) => apiFetch(`/buyer/orders/${orderId}/cancel`, { method: "PUT" }),
            getTracking: (orderId) => apiFetch(`/buyer/orders/${orderId}/tracking`),
            createPayment: (orderId) => apiFetch(`/buyer/payments/order/${orderId}`, { method: "POST" }),
            markPaymentSuccessful: (orderId) => apiFetch(`/buyer/payments/order/${orderId}/success`, { method: "PUT" }),
            getPayment: (orderId) => apiFetch(`/buyer/payments/order/${orderId}`)
        },
        seller: {
            getDashboard: () => apiFetch("/seller/dashboard"),
            getOrders: () => apiFetch("/seller/orders"),
            getAnalytics: () => apiFetch("/seller/analytics")
        },
        admin: {
            getDashboard: () => apiFetch("/admin/dashboard"),
            getUsers: () => apiFetch("/admin/users"),
            getUserById: (id) => apiFetch(`/admin/users/${id}`),
            createUser: (userData) => apiFetch("/admin/users", { method: "POST", body: userData }),
            updateUser: (id, userData) => apiFetch(`/admin/users/${id}`, { method: "PUT", body: userData }),
            deactivateUser: (id) => apiFetch(`/admin/users/${id}/deactivate`, { method: "PUT" }),
            getProducts: () => apiFetch("/admin/products"),
            createProduct: (product) => apiFetch("/admin/products", { method: "POST", body: product }),
            updateProduct: (id, product) => apiFetch(`/admin/products/${id}`, { method: "PUT", body: product }),
            deleteProduct: (id) => apiFetch(`/admin/products/${id}`, { method: "DELETE" }),
            getOrders: () => apiFetch("/admin/orders"),
            getOrderById: (orderId) => apiFetch(`/admin/orders/${orderId}`),
            updateOrderStatus: (orderId, status) => apiFetch(`/admin/orders/${orderId}/status?status=${encodeURIComponent(status)}`, { method: "PUT" }),
            cancelOrder: (orderId) => apiFetch(`/admin/orders/${orderId}/cancel`, { method: "PUT" }),
            updateTracking: (orderId, status, description = "") => apiFetch(`/admin/orders/${orderId}/tracking?status=${encodeURIComponent(status)}&description=${encodeURIComponent(description)}`, { method: "PUT" }),
            getAllActivity: () => apiFetch("/admin/activity"),
            getMyActivity: () => apiFetch("/admin/activity/my")
        }
    };
}

/* ================================
   Initialize
================================ */

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", function () {
        updateAuthUI();
    });
} else {
    updateAuthUI();
}