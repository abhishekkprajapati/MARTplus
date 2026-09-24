/* =========================================================
   SHOPSPHERE — CART JAVASCRIPT
   ========================================================= */


/* =========================================================
   CART CONFIGURATION
   ========================================================= */

const CART_STORAGE_KEY = "shopsphere_cart";

const FREE_SHIPPING_LIMIT = 999;

const STANDARD_SHIPPING = 79;


/* =========================================================
   CART STATE
   ========================================================= */

let appliedCoupon = null;

let selectedItems = new Set();


/* =========================================================
   DOM ELEMENTS
   ========================================================= */

const cartItemsContainer =
    document.getElementById("cartItems");

const cartSubtotalElement =
    document.getElementById("cartSubtotal");

const cartShippingElement =
    document.getElementById("cartShipping");

const cartDiscountElement =
    document.getElementById("cartDiscount");

const cartTotalElement =
    document.getElementById("cartTotal");

const cartItemSummary =
    document.getElementById("cartItemSummary");

const selectAllCart =
    document.getElementById("selectAllCart");

const checkoutButton =
    document.getElementById("checkoutButton");

const couponForm =
    document.getElementById("couponForm");

const couponInput =
    document.getElementById("couponInput");

const couponMessage =
    document.getElementById("couponMessage");

const cartRecommendations =
    document.getElementById("cartRecommendations");

const clearCartBtn =
    document.getElementById("clearCartBtn");

const cartConfirmModal =
    document.getElementById("cartConfirmModal");

const cartConfirmCancel =
    document.getElementById("cartConfirmCancel");

const cartConfirmDelete =
    document.getElementById("cartConfirmDelete");


/* =========================================================
   STORAGE
   ========================================================= */

function getCart() {

    try {

        const cart =
            localStorage.getItem(
                CART_STORAGE_KEY
            );


        return cart
            ? JSON.parse(cart)
            : [];

    } catch (error) {

        console.error(
            "Unable to load cart:",
            error
        );

        return [];
    }
}


function saveCart(cart) {

    try {

        localStorage.setItem(
            CART_STORAGE_KEY,
            JSON.stringify(cart)
        );

    } catch (error) {

        console.error(
            "Unable to save cart:",
            error
        );
    }
}


/* =========================================================
   FORMAT PRICE
   ========================================================= */

function formatCartPrice(price) {

    return new Intl.NumberFormat(
        "en-IN",
        {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 0
        }
    ).format(price);
}


/* =========================================================
   GET PRODUCT DETAILS
   ========================================================= */

function getProductDetails(productId) {

    if (
        typeof products === "undefined"
    ) {
        return null;
    }


    return products.find(
        product =>
            Number(product.id) ===
            Number(productId)
    );
}


/* =========================================================
   RENDER EMPTY CART
   ========================================================= */

function renderEmptyCart() {

    if (!cartItemsContainer) {
        return;
    }


    cartItemsContainer.innerHTML = `

        <div class="empty-cart">

            <div class="empty-cart-icon">
                🛒
            </div>

            <h2>
                Your cart is empty
            </h2>

            <p>
                Looks like you haven't added anything
                to your cart yet. Explore our products
                and find something you love.
            </p>

            <a
                href="products.html"
                class="btn btn-primary"
            >
                Start Shopping →
            </a>

        </div>

    `;


    updateCartSummary([]);


    if (selectAllCart) {
        selectAllCart.checked = false;
        selectAllCart.disabled = true;
    }


    if (checkoutButton) {

        checkoutButton.style.pointerEvents =
            "none";

        checkoutButton.style.opacity =
            "0.5";
    }


    if (clearCartBtn) {
        clearCartBtn.disabled = true;
        clearCartBtn.style.display = "none";
    }


    updateCartItemSummary(0);
}


/* =========================================================
   CREATE CART ITEM
   ========================================================= */

function createCartItem(
    item,
    index
) {

    const product =
        getProductDetails(
            item.productId
        );


    const name =
        product?.name ||
        item.name ||
        "Unknown Product";


    const category =
        product?.category ||
        "Product";


    const seller =
        product?.seller ||
        item.seller ||
        "MARTplus Seller";


    const emoji =
        product?.emoji ||
        item.emoji ||
        "🛍️";


    const price =
        Number(
            product?.price ??
            item.price ??
            0
        );


    const oldPrice =
        Number(
            product?.oldPrice ??
            price
        );


    const stock =
        Number(
            product?.stock ??
            10
        );


    const quantity =
        Math.max(
            1,
            Number(item.quantity || 1)
        );


    const isSelected =
        selectedItems.has(
            Number(item.productId)
        );


    return `

        <article
            class="cart-item"
            data-index="${index}"
            data-product-id="${item.productId}"
        >

            <input
                type="checkbox"
                class="cart-checkbox"
                data-product-id="${item.productId}"
                ${isSelected ? "checked" : ""}
            >


            <div class="cart-product-image">
                ${product?.imageUrl || product?.image
                    ? `<img src="${product.imageUrl || product.image}" alt="${name}" onerror="this.style.display='none';if(this.nextElementSibling)this.nextElementSibling.style.display='block';"><span class="cart-emoji" style="display:none;">${emoji}</span>`
                    : `<span class="cart-emoji">${emoji}</span>`
                }
            </div>


            <div class="cart-product-info">

                <span class="cart-product-category">
                    ${category}
                </span>


                <h3 class="cart-product-name">
                    ${name}
                </h3>


                <p class="cart-product-seller">
                    Sold by
                    <strong>${seller}</strong>
                </p>


                <span class="cart-stock">

                    <span class="stock-dot"></span>

                    ${stock > 0
            ? `${stock} available`
            : "Out of stock"
        }

                </span>


                <div class="cart-product-actions">

                    <button
                        type="button"
                        class="cart-action-btn remove"
                        data-action="remove"
                        data-product-id="${item.productId}"
                    >
                        Remove
                    </button>


                    <button
                        type="button"
                        class="cart-action-btn"
                        data-action="save"
                        data-product-id="${item.productId}"
                    >
                        Move to Wishlist
                    </button>

                </div>

            </div>


            <div>

                <div class="quantity-control">

                    <button
                        type="button"
                        class="quantity-btn"
                        data-action="decrease"
                        data-product-id="${item.productId}"
                        aria-label="Decrease quantity"
                    >
                        −
                    </button>


                    <span class="quantity-value">
                        ${quantity}
                    </span>


                    <button
                        type="button"
                        class="quantity-btn"
                        data-action="increase"
                        data-product-id="${item.productId}"
                        aria-label="Increase quantity"
                    >
                        +
                    </button>

                </div>

            </div>


            <div class="cart-product-price">

                <div class="cart-current-price">
                    ${formatCartPrice(
            price * quantity
        )}
                </div>

                ${oldPrice > price
            ? `
                            <div class="cart-old-price">
                                ${formatCartPrice(
                oldPrice * quantity
            )}
                            </div>
                        `
            : ""
        }

            </div>

        </article>

    `;
}


/* =========================================================
   RENDER CART
   ========================================================= */

function renderCart() {

    const cart =
        getCart();


    if (!cart.length) {

        renderEmptyCart();

        renderRecommendations();

        return;
    }


    if (selectAllCart) {
        selectAllCart.disabled = false;
    }


    if (clearCartBtn) {
        clearCartBtn.disabled = false;
        clearCartBtn.style.display = "inline-flex";
    }


    /*
       If this is the first render,
       select every item.
    */

    if (
        selectedItems.size === 0
    ) {

        cart.forEach(item => {

            selectedItems.add(
                Number(item.productId)
            );

        });
    }


    /*
       Remove selected IDs that no longer
       exist in the cart.
    */

    const cartIds =
        cart.map(
            item =>
                Number(item.productId)
        );


    selectedItems =
        new Set(
            [...selectedItems].filter(
                id =>
                    cartIds.includes(id)
            )
        );


    cartItemsContainer.innerHTML =
        cart
            .map(
                (item, index) =>
                    createCartItem(
                        item,
                        index
                    )
            )
            .join("");


    updateSelectAllState();

    updateCartSummary(cart);

    updateCartItemSummary(
        cart.length
    );

    renderRecommendations();
}


/* =========================================================
   UPDATE ITEM SUMMARY TEXT
   ========================================================= */

function updateCartItemSummary(
    itemCount
) {

    if (!cartItemSummary) {
        return;
    }


    if (itemCount === 0) {

        cartItemSummary.textContent =
            "Your shopping cart is currently empty.";

        return;
    }


    const cart =
        getCart();


    const totalQuantity =
        cart.reduce(
            (
                total,
                item
            ) =>
                total +
                Number(
                    item.quantity || 1
                ),
            0
        );


    cartItemSummary.textContent =
        `${totalQuantity} ${totalQuantity === 1
            ? "item"
            : "items"
        } in your cart. Review your selected products before checkout.`;
}


/* =========================================================
   GET SELECTED CART ITEMS
   ========================================================= */

function getSelectedCartItems() {

    const cart =
        getCart();


    return cart.filter(
        item =>
            selectedItems.has(
                Number(item.productId)
            )
    );
}


/* =========================================================
   UPDATE CART SUMMARY
   ========================================================= */

function updateCartSummary(
    cart
) {

    const selectedCart =
        cart.filter(
            item =>
                selectedItems.has(
                    Number(item.productId)
                )
        );


    let subtotal = 0;


    selectedCart.forEach(
        item => {

            const product =
                getProductDetails(
                    item.productId
                );


            const price =
                Number(
                    product?.price ??
                    item.price ??
                    0
                );


            const quantity =
                Number(
                    item.quantity || 1
                );


            subtotal +=
                price * quantity;
        }
    );


    /*
       Shipping
    */

    let shipping = 0;


    if (
        subtotal > 0 &&
        subtotal < FREE_SHIPPING_LIMIT
    ) {

        shipping =
            STANDARD_SHIPPING;
    }


    /*
       Coupon discount
    */

    const discount =
        calculateDiscount(
            subtotal
        );


    const total =
        Math.max(
            0,
            subtotal +
            shipping -
            discount
        );


    if (cartSubtotalElement) {

        cartSubtotalElement.textContent =
            formatCartPrice(
                subtotal
            );
    }


    if (cartShippingElement) {

        cartShippingElement.textContent =
            shipping === 0
                ? "FREE"
                : formatCartPrice(
                    shipping
                );
    }


    if (cartDiscountElement) {

        cartDiscountElement.textContent =
            discount > 0
                ? `- ${formatCartPrice(
                    discount
                )}`
                : "- ₹0";
    }


    if (cartTotalElement) {

        cartTotalElement.textContent =
            formatCartPrice(
                total
            );
    }


    if (checkoutButton) {

        const hasSelectedItems =
            selectedCart.length > 0;


        checkoutButton.style.pointerEvents =
            hasSelectedItems
                ? "auto"
                : "none";


        checkoutButton.style.opacity =
            hasSelectedItems
                ? "1"
                : "0.5";


        if (hasSelectedItems) {

            sessionStorage.setItem(
                "shopsphere_checkout_total",
                String(total)
            );

        } else {

            sessionStorage.removeItem(
                "shopsphere_checkout_total"
            );
        }
    }
}


/* =========================================================
   CALCULATE DISCOUNT
   ========================================================= */

function calculateDiscount(
    subtotal
) {

    if (
        !appliedCoupon ||
        subtotal <= 0
    ) {
        return 0;
    }


    if (
        appliedCoupon.type === "percentage"
    ) {

        return Math.round(
            subtotal *
            appliedCoupon.value /
            100
        );
    }


    if (
        appliedCoupon.type === "fixed"
    ) {

        return Math.min(
            appliedCoupon.value,
            subtotal
        );
    }


    return 0;
}


/* =========================================================
   UPDATE SELECT ALL
   ========================================================= */

function updateSelectAllState() {

    if (!selectAllCart) {
        return;
    }


    const cart =
        getCart();


    if (!cart.length) {

        selectAllCart.checked = false;

        return;
    }


    const allSelected =
        cart.every(
            item =>
                selectedItems.has(
                    Number(item.productId)
                )
        );


    selectAllCart.checked =
        allSelected;
}


/* =========================================================
   SELECT / UNSELECT ITEM
   ========================================================= */

function toggleCartItemSelection(
    productId,
    checked
) {

    const id =
        Number(productId);


    if (checked) {

        selectedItems.add(id);

    } else {

        selectedItems.delete(id);
    }


    updateSelectAllState();

    updateCartSummary(
        getCart()
    );
}


/* =========================================================
   SELECT ALL
   ========================================================= */

selectAllCart?.addEventListener(
    "change",
    function () {

        const cart =
            getCart();


        if (this.checked) {

            cart.forEach(item => {

                selectedItems.add(
                    Number(
                        item.productId
                    )
                );

            });

        } else {

            selectedItems.clear();
        }


        renderCart();
    }
);


/* =========================================================
   CLEAR CART & CONFIRM MODAL EVENTS
   ========================================================= */

clearCartBtn?.addEventListener(
    "click",
    function () {

        const cart = getCart();

        if (!cart.length) {
            return;
        }

        cartConfirmModal?.classList.add(
            "show"
        );

    }
);


cartConfirmCancel?.addEventListener(
    "click",
    function () {

        cartConfirmModal?.classList.remove(
            "show"
        );

    }
);


cartConfirmDelete?.addEventListener(
    "click",
    function () {

        cartConfirmModal?.classList.remove(
            "show"
        );

        clearCart();

    }
);


cartConfirmModal?.addEventListener(
    "click",
    function (event) {

        if (
            event.target ===
            cartConfirmModal
        ) {

            cartConfirmModal.classList.remove(
                "show"
            );

        }

    }
);


document.addEventListener(
    "keydown",
    function (event) {

        if (
            event.key === "Escape" &&
            cartConfirmModal?.classList.contains("show")
        ) {

            cartConfirmModal.classList.remove(
                "show"
            );

        }

    }
);


/* =========================================================
   CART ITEM EVENTS
   ========================================================= */

cartItemsContainer?.addEventListener(
    "click",
    function (event) {

        const button =
            event.target.closest(
                "[data-action]"
            );


        if (!button) {
            return;
        }


        const action =
            button.dataset.action;


        const productId =
            Number(
                button.dataset.productId
            );


        if (
            action === "increase"
        ) {

            changeQuantity(
                productId,
                1
            );

            return;
        }


        if (
            action === "decrease"
        ) {

            changeQuantity(
                productId,
                -1
            );

            return;
        }


        if (
            action === "remove"
        ) {

            removeCartItem(
                productId
            );

            return;
        }


        if (
            action === "save"
        ) {

            moveToWishlist(
                productId
            );

            return;
        }
    }
);


/* =========================================================
   CHECKBOX EVENTS
   ========================================================= */

cartItemsContainer?.addEventListener(
    "change",
    function (event) {

        const checkbox =
            event.target.closest(
                ".cart-checkbox"
            );


        if (!checkbox) {
            return;
        }


        toggleCartItemSelection(
            checkbox.dataset.productId,
            checkbox.checked
        );
    }
);


/* =========================================================
   CHANGE QUANTITY
   ========================================================= */

function changeQuantity(
    productId,
    amount
) {

    const cart =
        getCart();


    const item =
        cart.find(
            cartItem =>
                Number(
                    cartItem.productId
                ) === Number(productId)
        );


    if (!item) {
        return;
    }


    const product =
        getProductDetails(
            productId
        );


    const maxStock =
        Number(
            product?.stock ??
            99
        );


    const newQuantity =
        Number(
            item.quantity || 1
        ) + amount;


    if (newQuantity < 1) {

        removeCartItem(
            productId
        );

        return;
    }


    if (
        newQuantity > maxStock
    ) {

        window.ShopSphere?.showToast(
            "Stock Limit",
            `Only ${maxStock} units are available.`
        );

        return;
    }


    item.quantity =
        newQuantity;

    saveCart(cart);

    renderCart();

    window.ShopSphere?.updateCartCount();

    if (window.ShopSphereAuth && window.ShopSphereAuth.isBuyer()) {
        try {
            const api = window.ShopSphereApi;
            if (api && item.cartItemId) {
                api.buyer.updateCartItem(item.cartItemId, newQuantity).catch(() => {});
            }
        } catch (e) {}
    }
}


/* =========================================================
   REMOVE CART ITEM
   ========================================================= */

function removeCartItem(
    productId
) {

    const cart =
        getCart();

    const product =
        getProductDetails(
            productId
        );

    const targetItem =
        cart.find(
            item =>
                Number(item.productId) === Number(productId)
        );

    const updatedCart =
        cart.filter(
            item =>
                Number(
                    item.productId
                ) !== Number(productId)
        );

    selectedItems.delete(
        Number(productId)
    );

    saveCart(
        updatedCart
    );

    renderCart();

    window.ShopSphere?.updateCartCount();

    if (window.ShopSphereAuth && window.ShopSphereAuth.isBuyer()) {
        try {
            const api = window.ShopSphereApi;
            if (api && targetItem?.cartItemId) {
                api.buyer.removeCartItem(targetItem.cartItemId).catch(() => {});
            }
        } catch (e) {}
    }

    window.ShopSphere?.showToast(
        "Removed from Cart",
        `${product?.name ||
        "Product"
        } was removed from your cart.`
    );
}


/* =========================================================
   CLEAR ALL CART ITEMS
   ========================================================= */

function clearCart() {

    saveCart([]);

    selectedItems.clear();

    sessionStorage.removeItem(
        "appliedCoupon"
    );

    appliedCoupon = null;

    renderCart();

    window.ShopSphere?.updateCartCount();

    if (window.ShopSphereAuth && window.ShopSphereAuth.isBuyer()) {
        try {
            const api = window.ShopSphereApi;
            if (api) {
                api.buyer.clearCart().catch(() => {});
            }
        } catch (e) {}
    }

    window.ShopSphere?.showToast(
        "Cart Cleared 🗑️",
        "All products have been removed from your cart."
    );
}


/* =========================================================
   MOVE TO WISHLIST
   ========================================================= */

function moveToWishlist(
    productId
) {

    const cart =
        getCart();


    const item =
        cart.find(
            cartItem =>
                Number(
                    cartItem.productId
                ) === Number(productId)
        );


    if (!item) {
        return;
    }


    const wishlist =
        window.ShopSphere?.getStorageData(
            "shopsphere_wishlist",
            []
        ) || [];


    const alreadyExists =
        wishlist.some(
            wishlistItem =>
                Number(
                    wishlistItem.productId
                ) === Number(productId)
        );


    if (!alreadyExists) {

        const product =
            getProductDetails(
                productId
            );


        wishlist.push({

            productId:
                Number(productId),

            name:
                product?.name ||
                item.name,

            price:
                product?.price ||
                item.price,

            emoji:
                product?.emoji ||
                item.emoji,

            seller:
                product?.seller ||
                item.seller

        });


        window.ShopSphere?.saveStorageData(
            "shopsphere_wishlist",
            wishlist
        );


        window.ShopSphere?.updateWishlistCount();
    }


    removeCartItem(
        productId
    );


    window.ShopSphere?.showToast(
        "Moved to Wishlist ❤️",
        "The product has been saved to your wishlist."
    );
}


/* =========================================================
   COUPON CODES
   ========================================================= */

const availableCoupons = {

    SAVE10: {
        type: "percentage",
        value: 10,
        minimum: 999
    },

    SAVE200: {
        type: "fixed",
        value: 200,
        minimum: 1999
    },

    WELCOME: {
        type: "percentage",
        value: 15,
        minimum: 1499
    }

};


/* =========================================================
   APPLY COUPON
   ========================================================= */

couponForm?.addEventListener(
    "submit",
    function (event) {

        event.preventDefault();


        const code =
            couponInput?.value
                .trim()
                .toUpperCase();


        if (!code) {

            showCouponMessage(
                "Please enter a coupon code.",
                "error"
            );

            return;
        }


        const coupon =
            availableCoupons[code];


        if (!coupon) {

            showCouponMessage(
                "Invalid coupon code.",
                "error"
            );

            return;
        }


        const selectedCart =
            getSelectedCartItems();


        let subtotal = 0;


        selectedCart.forEach(
            item => {

                const product =
                    getProductDetails(
                        item.productId
                    );


                const price =
                    Number(
                        product?.price ??
                        item.price ??
                        0
                    );


                subtotal +=
                    price *
                    Number(
                        item.quantity || 1
                    );
            }
        );


        if (
            subtotal <
            coupon.minimum
        ) {

            showCouponMessage(
                `Minimum order value is ${formatCartPrice(
                    coupon.minimum
                )}.`,
                "error"
            );

            return;
        }


        appliedCoupon = {

            code,

            type:
                coupon.type,

            value:
                coupon.value

        };


        showCouponMessage(
            `Coupon ${code} applied successfully.`,
            "success"
        );


        updateCartSummary(
            getCart()
        );
    }
);


/* =========================================================
   COUPON MESSAGE
   ========================================================= */

function showCouponMessage(
    message,
    type
) {

    if (!couponMessage) {
        return;
    }


    couponMessage.textContent =
        message;


    couponMessage.className =
        `coupon-message show ${type}`;
}


/* =========================================================
   RECOMMENDATIONS
   ========================================================= */

function renderRecommendations() {

    if (
        !cartRecommendations ||
        typeof products === "undefined"
    ) {
        return;
    }


    const cart =
        getCart();


    const cartIds =
        cart.map(
            item =>
                Number(item.productId)
        );


    const recommendations =
        products
            .filter(
                product =>
                    !cartIds.includes(
                        Number(product.id)
                    )
            )
            .slice(0, 4);


    if (
        typeof createProductCard ===
        "function"
    ) {

        cartRecommendations.innerHTML =
            recommendations
                .map(
                    product =>
                        createProductCard(
                            product
                        ).outerHTML
                )
                .join("");

    }
}


/* =========================================================
   CHECKOUT PROTECTION
   ========================================================= */

checkoutButton?.addEventListener(
    "click",
    function (event) {

        const selected =
            getSelectedCartItems();


        if (!selected.length) {

            event.preventDefault();


            window.ShopSphere?.showToast(
                "Cart is Empty",
                "Select at least one product before checkout."
            );

            return;
        }


        /*
           Save selected items for checkout.
        */

        sessionStorage.setItem(
            "shopsphere_checkout_items",
            JSON.stringify(
                selected
            )
        );
    }
);


/* =========================================================
   SYNC CART COUNTS
   ========================================================= */

function syncCartPage() {

    const cart =
        getCart();


    if (
        window.ShopSphere &&
        typeof window.ShopSphere
            .updateCartCount ===
        "function"
    ) {

        window.ShopSphere
            .updateCartCount();
    }


    if (!cart.length) {

        selectedItems.clear();

    }
}


/* =========================================================
   SYNC CART FROM BACKEND API
   ========================================================= */

async function loadCartFromBackend() {
    if (window.ShopSphereAuth && window.ShopSphereAuth.isBuyer()) {
        try {
            const api = window.ShopSphereApi;
            if (!api) return;
            const backendItems = await api.buyer.getCart();
            if (Array.isArray(backendItems)) {
                if (backendItems.length > 0) {
                    const mapped = backendItems.map(item => {
                        let emoji = "📦";
                        const n = (item.productName || "").toLowerCase();
                        if (n.includes("phone") || n.includes("mobile") || n.includes("apple")) emoji = "📱";
                        else if (n.includes("headphone") || n.includes("audio") || n.includes("speaker") || n.includes("sound")) emoji = "🎧";
                        else if (n.includes("watch")) emoji = "⌚";
                        else if (n.includes("mouse") || n.includes("keyboard") || n.includes("laptop")) emoji = "🖱️";
                        else if (n.includes("shoe") || n.includes("sneaker")) emoji = "👟";
                        else if (n.includes("bag") || n.includes("backpack")) emoji = "🎒";
                        else if (n.includes("lamp") || n.includes("light")) emoji = "💡";

                        return {
                            cartItemId: item.id,
                            productId: item.productId,
                            name: item.productName,
                            price: Number(item.price),
                            quantity: item.quantity,
                            emoji: emoji,
                            seller: "Verified Seller"
                        };
                    });
                    saveCart(mapped);
                    renderCart();
                    syncCartPage();
                } else {
                    const localCart = getCart();
                    if (localCart.length > 0) {
                        for (const item of localCart) {
                            try {
                                const res = await api.buyer.addToCart(item.productId, item.quantity || 1);
                                if (res && res.id) item.cartItemId = res.id;
                            } catch (e) {}
                        }
                        saveCart(localCart);
                    }
                }
            }
        } catch (e) {
            console.warn("Could not sync cart with backend:", e);
        }
    }
}

/* =========================================================
   INITIALIZE CART
   ========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    function () {

        syncCartPage();

        renderCart();

        loadCartFromBackend();

    }
);


/* =========================================================
   GLOBAL CART API
   ========================================================= */

window.ShopSphereCart = {

    getCart,

    saveCart,

    renderCart,

    changeQuantity,

    removeCartItem,

    clearCart,

    moveToWishlist,

    getSelectedCartItems,

    calculateDiscount,

    loadCartFromBackend

};